
import { useEffect, useState } from 'react';
import {
  listarCampanhasAdmin,
  listarParticipantesElegiveis,
  executarSorteio,
} from '../../services/api.js';
import './Sorteios.css';

function embaralhar(lista) {
  const copia = [...lista];

  for (let i = copia.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [copia[i], copia[j]] = [copia[j], copia[i]];
  }

  return copia;
}

export default function Sorteios() {
  const [campanhas, setCampanhas] = useState([]);
  const [campanhaId, setCampanhaId] = useState('');
  const [participantes, setParticipantes] = useState([]);
  const [premio, setPremio] = useState('');
  const [quantidade, setQuantidade] = useState(1);

  const [carregandoCampanhas, setCarregandoCampanhas] = useState(true);
  const [carregandoParticipantes, setCarregandoParticipantes] = useState(false);
  const [sorteando, setSorteando] = useState(false);
  const [apresentacao, setApresentacao] = useState(false);
  const [nomesAnimacao, setNomesAnimacao] = useState([]);
  const [resultado, setResultado] = useState(null);
  const [erro, setErro] = useState('');

  useEffect(() => {
    listarCampanhasAdmin()
      .then(setCampanhas)
      .catch((e) => setErro(e.message))
      .finally(() => setCarregandoCampanhas(false));
  }, []);

  useEffect(() => {
    function observarTelaCheia() {
      if (!document.fullscreenElement) {
        setApresentacao(false);
      }
    }

    document.addEventListener('fullscreenchange', observarTelaCheia);
    return () => document.removeEventListener('fullscreenchange', observarTelaCheia);
  }, []);

  async function selecionarCampanha(id) {
    setCampanhaId(id);
    setParticipantes([]);
    setResultado(null);
    setErro('');

    if (!id) return;

    setCarregandoParticipantes(true);

    try {
      const lista = await listarParticipantesElegiveis(id);
      setParticipantes(lista);
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregandoParticipantes(false);
    }
  }

  async function iniciarSorteio(event) {
    event.preventDefault();
    setErro('');
    setResultado(null);

    if (!campanhaId) {
      setErro('Selecione uma campanha.');
      return;
    }

    if (!premio.trim()) {
      setErro('Informe o prêmio.');
      return;
    }

    if (
      Number(quantidade) < 1 ||
      Number(quantidade) > participantes.length
    ) {
      setErro('A quantidade de ganhadores não pode ser maior que o número de participantes elegíveis.');
      return;
    }

    // Solicita tela cheia diretamente no clique do operador.
    try {
      await document.documentElement.requestFullscreen();
      setApresentacao(true);
    } catch {
      setErro('Não foi possível abrir a tela cheia. Verifique as permissões do navegador.');
      return;
    }

    setSorteando(true);

    try {
      // A escolha oficial é feita pelo backend.
      const resposta = await executarSorteio({
        campanhaId: Number(campanhaId),
        premio: premio.trim(),
        quantidadeGanhadores: Number(quantidade),
      });

      // A lista embaralhada serve apenas para a animação.
      setNomesAnimacao(
        embaralhar(participantes.map((p) => p.nomeExibicao))
      );

      window.setTimeout(() => {
        setResultado(resposta);
        setSorteando(false);
      }, 6500);
    } catch (e) {
      setErro(e.message);
      setSorteando(false);
      setApresentacao(false);

      if (document.fullscreenElement) {
        await document.exitFullscreen();
      }
    }
  }

  async function voltarAoPainel() {
    setApresentacao(false);
    setResultado(null);

    if (document.fullscreenElement) {
      await document.exitFullscreen();
    }
  }

  const campanhaSelecionada = campanhas.find(
    (c) => String(c.id) === campanhaId
  );

  return (
    <section className="pagina-sorteios">
      <h1 className="titulo-painel">Sorteios</h1>
      <p className="intro">
        Prepare e execute o sorteio de uma campanha.
      </p>

      <form className="sorteio-formulario" onSubmit={iniciarSorteio}>
        <label htmlFor="campanha">Campanha</label>

        <select
          id="campanha"
          value={campanhaId}
          onChange={(e) => selecionarCampanha(e.target.value)}
          disabled={carregandoCampanhas || sorteando}
          required
        >
          <option value="">
            {carregandoCampanhas
              ? 'Carregando campanhas...'
              : 'Selecione uma campanha'}
          </option>

          {campanhas.map((campanha) => (
            <option key={campanha.id} value={campanha.id}>
              {campanha.nome} — {campanha.status}
            </option>
          ))}
        </select>

        {campanhaSelecionada && (
          <p className="sorteio-campanha-info">
            Campanha selecionada: <strong>{campanhaSelecionada.nome}</strong>
          </p>
        )}

        <label htmlFor="premio">Prêmio</label>

        <input
          id="premio"
          type="text"
          maxLength={150}
          placeholder="Ex.: Vale-compras de R$ 500"
          value={premio}
          onChange={(e) => setPremio(e.target.value)}
          disabled={sorteando}
          required
        />

        <label htmlFor="quantidade">Quantidade de ganhadores</label>

        <input
          id="quantidade"
          type="number"
          min="1"
          max={participantes.length || 1}
          value={quantidade}
          onChange={(e) => setQuantidade(e.target.value)}
          disabled={sorteando}
          required
        />

        <div className="sorteio-contagem">
          {carregandoParticipantes
            ? 'Buscando participantes...'
            : `${participantes.length} participantes elegíveis`}
        </div>

        <button
          className="sorteio-iniciar"
          type="submit"
          disabled={
            sorteando ||
            carregandoParticipantes ||
            !campanhaId ||
            participantes.length === 0
          }
        >
          Iniciar sorteio em tela cheia
        </button>
      </form>

      {erro && (
        <p className="sorteio-erro" role="alert">
          {erro}
        </p>
      )}

      {apresentacao && (
        <section className="sorteio-apresentacao" aria-live="polite">
          <div className="sorteio-apresentacao-topo">
            <span>METALAB • SORTEIO OFICIAL</span>
            <button
              type="button"
              onClick={voltarAoPainel}
              disabled={sorteando}
            >
              Sair da apresentação
            </button>
          </div>

          <div className="sorteio-apresentacao-conteudo">
            <p className="sorteio-etiqueta">
              {resultado ? 'RESULTADO OFICIAL' : 'SORTEIO EM ANDAMENTO'}
            </p>

            <h2>
              {resultado ? 'Temos nossos ganhadores!' : 'Boa sorte a todos!'}
            </h2>

            <p className="sorteio-apresentacao-premio">
              {premio}
            </p>

            <div className={`sorteio-visor ${sorteando ? 'sorteio-visor-animando' : ''}`}>
              {sorteando ? (
                <div className="sorteio-lista-animada">
                  {[...nomesAnimacao, ...nomesAnimacao, ...nomesAnimacao].map(
                    (nome, indice) => (
                      <div className="sorteio-nome-animado" key={indice}>
                        {nome}
                      </div>
                    )
                  )}
                </div>
              ) : resultado ? (
                <div className="sorteio-ganhadores">
                  {resultado.ganhadores.map((ganhador) => (
                    <div className="sorteio-ganhador" key={ganhador.posicao}>
                      <span>{ganhador.posicao}º lugar</span>
                      <strong>{ganhador.nomeExibicao}</strong>
                    </div>
                  ))}
                  <p className="sorteio-premio">
                    Prêmio: {resultado.premio}
                  </p>
                  <p className="sorteio-numero">
                    Sorteio nº {resultado.numero}
                  </p>
                </div>
              ) : (
                <p>Preparando o sorteio...</p>
              )}
            </div>
          </div>

          {resultado && (
            <button
              className="sorteio-voltar"
              type="button"
              onClick={voltarAoPainel}
            >
              Voltar ao painel
            </button>
          )}
        </section>
      )}
    </section>
  );
}
