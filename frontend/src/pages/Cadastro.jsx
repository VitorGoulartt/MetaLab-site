import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import Aviso from '../components/Aviso.jsx';
import Campo from '../components/Campo.jsx';
import Moldura from '../components/Moldura.jsx';
import Turnstile from '../components/Turnstile.jsx';
import { ApiError, buscarCampanha, participar } from '../services/api.js';
import { mascararCpf, mascararTelefone } from '../utils/mascaras.js';
import { ORDEM, validar, validarCampo } from '../utils/validacao.js';

const SITE_KEY = import.meta.env.VITE_TURNSTILE_SITE_KEY;
const VAZIO = { nome: '', email: '', telefone: '', cpf: '', faixaEtaria: '', responsavelNome: '', aceite: false };

export default function Cadastro() {
  const { slug } = useParams();
  const [estado, setEstado] = useState('carregando'); // carregando | pronto | encerrada | naoEncontrada | falha
  const [campanha, setCampanha] = useState(null);
  const [tentativa, setTentativa] = useState(0);

  const [valores, setValores] = useState(VAZIO);
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState('');
  const [token, setToken] = useState('');
  const [resetCaptcha, setResetCaptcha] = useState(0);
  const [enviando, setEnviando] = useState(false);
  const [resultado, setResultado] = useState(null); // { tipo: 'ok' | 'duplicado', nome }

  useEffect(() => {
    let ativo = true;
    setEstado('carregando');
    buscarCampanha(slug)
      .then((c) => {
        if (!ativo) return;
        setCampanha(c);
        setEstado(c.aberta ? 'pronto' : 'encerrada');
      })
      .catch((e) => {
        if (ativo) setEstado(e.status === 404 ? 'naoEncontrada' : 'falha');
      });
    return () => {
      ativo = false;
    };
  }, [slug, tentativa]);

  function mudar(campo, valor) {
    setValores((v) => ({ ...v, [campo]: valor }));
    setErros((e) => {
      if (!e[campo]) return e;
      const { [campo]: _removido, ...resto } = e;
      return resto;
    });
  }

  function aoSair(campo) {
    const mensagem = valores[campo] ? validarCampo(campo, valores) : undefined;
    if (mensagem) setErros((e) => ({ ...e, [campo]: mensagem }));
  }

  function receberToken(t) {
    setToken(t);
    if (t) {
      setErros((e) => {
        const { captcha: _removido, ...resto } = e;
        return resto;
      });
    }
  }

  function focar(campo) {
    if (campo === 'captcha') {
      document.getElementById('captcha')?.scrollIntoView({ block: 'center' });
    } else {
      document.getElementById(`campo-${campo}`)?.focus();
    }
  }

  function tratarErro(err) {
    setResetCaptcha((n) => n + 1); // o token já foi gasto na tentativa
    if (!(err instanceof ApiError)) {
      setErroGeral('Algo deu errado. Tente de novo.');
      return;
    }
    if (err.status === 409) {
      setResultado({ tipo: 'duplicado' });
      return;
    }
    if (err.status === 404) {
      setEstado('encerrada');
      return;
    }
    if (err.status === 400) {
      const mapeados = {};
      for (const [chave, msg] of Object.entries(err.campos || {})) {
        mapeados[chave === 'captchaToken' ? 'captcha' : chave] = msg;
      }
      if (Object.keys(mapeados).length === 0) {
        if (err.message.includes('robô')) mapeados.captcha = err.message;
        else setErroGeral(err.message);
      }
      setErros(mapeados);
      const primeiro = ORDEM.find((k) => mapeados[k]);
      if (primeiro) setTimeout(() => focar(primeiro), 0);
      return;
    }
    setErroGeral(err.message);
  }

  async function enviar(evento) {
    evento.preventDefault();
    setErroGeral('');
    const novos = validar(valores, token);
    setErros(novos);
    const primeiro = ORDEM.find((k) => novos[k]);
    if (primeiro) {
      focar(primeiro);
      return;
    }
    setEnviando(true);
    try {
      await participar(slug, {
        nome: valores.nome.trim(),
        email: valores.email.trim(),
        telefone: valores.telefone,
        cpf: valores.cpf,
        faixaEtaria: valores.faixaEtaria,
        responsavelNome: valores.faixaEtaria === 'MENOR' ? valores.responsavelNome.trim() : null,
        aceite: valores.aceite,
        captchaToken: token,
      });
      setResultado({ tipo: 'ok', nome: valores.nome.trim().split(/\s+/)[0] });
    } catch (err) {
      tratarErro(err);
    } finally {
      setEnviando(false);
    }
  }

  function voltarAoFormulario(limpar) {
    if (limpar) setValores(VAZIO);
    setErros({});
    setErroGeral('');
    setToken('');
    setResultado(null);
  }

  if (estado === 'carregando') {
    return (
      <Moldura ocupado>
        <p className="intro">Carregando campanha…</p>
      </Moldura>
    );
  }
  if (estado === 'naoEncontrada') {
    return <Aviso titulo="Campanha não encontrada" texto="Confira o endereço ou escaneie o QR Code de novo." />;
  }
  if (estado === 'encerrada') {
    return (
      <Aviso
        titulo="Esta campanha não está recebendo participações"
        texto="O período de cadastro terminou ou ainda não começou."
      />
    );
  }
  if (estado === 'falha') {
    return (
      <Aviso titulo="Não foi possível carregar" texto="Verifique a internet e tente de novo.">
        <button type="button" className="botao" onClick={() => setTentativa((n) => n + 1)}>
          Tentar de novo
        </button>
      </Aviso>
    );
  }

  if (resultado?.tipo === 'ok') {
    return (
      <Moldura>
        <div className="selo" aria-hidden="true">
          <svg viewBox="0 0 24 24">
            <path d="M5 12.5l4.5 4.5L19 7.5" fill="none" stroke="currentColor" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </div>
        <div role="status">
          <h1 className="titulo">
            <span className="destaque">Participação confirmada</span>
          </h1>
          <p className="intro">
            Boa sorte, {resultado.nome}! Avisaremos o resultado pelo e-mail e pelo telefone que você informou.
          </p>
        </div>
        <button type="button" className="botao secundario" onClick={() => voltarAoFormulario(true)}>
          Cadastrar outra pessoa
        </button>
      </Moldura>
    );
  }

  if (resultado?.tipo === 'duplicado') {
    return (
      <Moldura>
        <div role="status">
          <h1 className="titulo">
            <span className="destaque">Você já está participando</span>
          </h1>
          <p className="intro">Este CPF já foi cadastrado nesta campanha. Não precisa fazer mais nada.</p>
        </div>
        <button type="button" className="botao secundario" onClick={() => voltarAoFormulario(false)}>
          Voltar ao formulário
        </button>
      </Moldura>
    );
  }

  const menor = valores.faixaEtaria === 'MENOR';
  const regulamento =
    campanha.regulamentoUrl && campanha.regulamentoUrl.startsWith('https://') ? (
      <a href={campanha.regulamentoUrl} target="_blank" rel="noopener noreferrer">
        regulamento da campanha
      </a>
    ) : (
      'regulamento da campanha'
    );

  return (
    <Moldura>
      <h1 className="titulo">
        <span className="destaque">{campanha.nome}</span>
      </h1>
      <p className="intro">Preencha seus dados e confirme. Leva menos de um minuto.</p>

      <form className="formulario" onSubmit={enviar} noValidate>
        <Campo
          id="nome"
          rotulo="Nome completo"
          type="text"
          autoComplete="name"
          enterKeyHint="next"
          value={valores.nome}
          onChange={(e) => mudar('nome', e.target.value)}
          onBlur={() => aoSair('nome')}
          erro={erros.nome}
        />
        <Campo
          id="email"
          rotulo="E-mail"
          type="email"
          autoComplete="email"
          inputMode="email"
          enterKeyHint="next"
          value={valores.email}
          onChange={(e) => mudar('email', e.target.value)}
          onBlur={() => aoSair('email')}
          erro={erros.email}
        />
        <Campo
          id="telefone"
          rotulo="Telefone com DDD"
          dica="Usaremos só para avisar o resultado."
          type="tel"
          autoComplete="tel-national"
          inputMode="tel"
          placeholder="(61) 99999-9999"
          enterKeyHint="next"
          value={valores.telefone}
          onChange={(e) => mudar('telefone', mascararTelefone(e.target.value))}
          onBlur={() => aoSair('telefone')}
          erro={erros.telefone}
        />
        <Campo
          id="cpf"
          rotulo="CPF"
          dica="Garante um cadastro por pessoa."
          type="text"
          autoComplete="off"
          inputMode="numeric"
          placeholder="000.000.000-00"
          enterKeyHint="done"
          value={valores.cpf}
          onChange={(e) => mudar('cpf', mascararCpf(e.target.value))}
          onBlur={() => aoSair('cpf')}
          erro={erros.cpf}
        />

        <fieldset className="grupo" aria-describedby={erros.faixaEtaria ? 'erro-faixaEtaria' : undefined}>
          <legend>Faixa etária</legend>
          <label className="opcao">
            <input
              id="campo-faixaEtaria"
              type="radio"
              name="faixaEtaria"
              value="ADULTO"
              checked={valores.faixaEtaria === 'ADULTO'}
              onChange={() => mudar('faixaEtaria', 'ADULTO')}
            />
            <span>Tenho 18 anos ou mais</span>
          </label>
          <label className="opcao">
            <input
              type="radio"
              name="faixaEtaria"
              value="MENOR"
              checked={menor}
              onChange={() => mudar('faixaEtaria', 'MENOR')}
            />
            <span>Tenho menos de 18 anos</span>
          </label>
          {erros.faixaEtaria && (
            <p className="erro" id="erro-faixaEtaria" role="alert">
              {erros.faixaEtaria}
            </p>
          )}
        </fieldset>

        {menor && (
          <Campo
            id="responsavelNome"
            rotulo="Nome completo do responsável"
            dica="Menores participam com a autorização de um responsável."
            type="text"
            autoComplete="off"
            value={valores.responsavelNome}
            onChange={(e) => mudar('responsavelNome', e.target.value)}
            onBlur={() => aoSair('responsavelNome')}
            erro={erros.responsavelNome}
          />
        )}

        <div className="campo">
          <label className="aceite" htmlFor="campo-aceite">
            <input
              id="campo-aceite"
              type="checkbox"
              checked={valores.aceite}
              onChange={(e) => mudar('aceite', e.target.checked)}
              aria-invalid={erros.aceite ? 'true' : 'false'}
              aria-describedby={erros.aceite ? 'erro-aceite' : undefined}
            />
            <span>
              Li e aceito o {regulamento} e a{' '}
              <Link to="/privacidade" target="_blank" rel="noopener noreferrer">
                política de privacidade
              </Link>
              {menor && ', e declaro que meu responsável autoriza a minha participação'}.
            </span>
          </label>
          {erros.aceite && (
            <p className="erro" id="erro-aceite" role="alert">
              {erros.aceite}
            </p>
          )}
        </div>

        <div className="campo" id="captcha">
          <Turnstile
            siteKey={SITE_KEY}
            onToken={receberToken}
            resetSignal={resetCaptcha}
            onErro={() =>
              setErros((e) => ({
                ...e,
                captcha: 'Não foi possível carregar a verificação anti-robô. Recarregue a página.',
              }))
            }
          />
          {erros.captcha && (
            <p className="erro" role="alert">
              {erros.captcha}
            </p>
          )}
        </div>

        {erroGeral && (
          <p className="aviso-erro" role="alert">
            {erroGeral}
          </p>
        )}

        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? 'Enviando…' : 'Confirmar participação'}
        </button>
        <p className="rodape">Seus dados são usados só nesta campanha e para avisar o resultado.</p>
      </form>
    </Moldura>
  );
}
