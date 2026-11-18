
import { useEffect, useState } from 'react';
import {
  listarCampanhasAdmin,
  criarCampanha,
  editarCampanha,
} from '../../services/api.js';
import './Campanhas.css';

const formularioInicial = {
  nome: '',
  slug: '',
  inicioEm: '',
  fimEm: '',
  status: 'RASCUNHO',
  regulamentoUrl: '',
};

function paraDataLocal(valor) {
  if (!valor) return '';
  const data = new Date(valor);
  const local = new Date(data.getTime() - data.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 16);
}

export default function Campanhas() {
  const [campanhas, setCampanhas] = useState([]);
  const [formulario, setFormulario] = useState(formularioInicial);
  const [editando, setEditando] = useState(null);
  const [carregando, setCarregando] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');

  async function carregar() {
    try {
      setCampanhas(await listarCampanhasAdmin());
    } catch (e) {
      setErro(e.message);
    } finally {
      setCarregando(false);
    }
  }

  useEffect(() => {
    carregar();
  }, []);

  function alterar(campo, valor) {
    setFormulario((atual) => ({ ...atual, [campo]: valor }));
  }

  function prepararEdicao(campanha) {
    setEditando(campanha.id);
    setFormulario({
      nome: campanha.nome,
      slug: campanha.slug,
      inicioEm: paraDataLocal(campanha.inicioEm),
      fimEm: paraDataLocal(campanha.fimEm),
      status: campanha.status,
      regulamentoUrl: '',
    });
    setErro('');
    setSucesso('');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function cancelarEdicao() {
    setEditando(null);
    setFormulario(formularioInicial);
    setErro('');
  }

  async function salvar(event) {
    event.preventDefault();
    setErro('');
    setSucesso('');

    if (!formulario.inicioEm || !formulario.fimEm) {
      setErro('Informe as datas de início e encerramento.');
      return;
    }

    const dados = {
      ...formulario,
      inicioEm: new Date(formulario.inicioEm).toISOString(),
      fimEm: new Date(formulario.fimEm).toISOString(),
      regulamentoUrl: formulario.regulamentoUrl.trim() || null,
    };

    setSalvando(true);

    try {
      if (editando) {
        await editarCampanha(editando, dados);
        setSucesso('Campanha atualizada com sucesso!');
      } else {
        await criarCampanha(dados);
        setSucesso('Campanha criada com sucesso!');
      }

      setFormulario(formularioInicial);
      setEditando(null);
      await carregar();
    } catch (e) {
      setErro(e.message);
    } finally {
      setSalvando(false);
    }
  }

  return (
    <section className="pagina-campanhas">
      <h1 className="titulo-painel">Campanhas</h1>
      <p className="intro">Crie e gerencie as campanhas do MetaLab.</p>

      <form className="campanha-formulario" onSubmit={salvar}>
        <h2>{editando ? 'Editar campanha' : 'Nova campanha'}</h2>

        <label htmlFor="nome">Nome da campanha</label>
        <input
          id="nome"
          value={formulario.nome}
          onChange={(e) => alterar('nome', e.target.value)}
          maxLength={150}
          required
        />

        <label htmlFor="slug">Endereço da campanha</label>
        <input
          id="slug"
          value={formulario.slug}
          onChange={(e) => alterar('slug', e.target.value)}
          placeholder="campanha-dia-21-11"
          maxLength={80}
          required
        />
        <small>Será usado no endereço público da campanha.</small>

        <div className="campanha-campos-duplos">
          <div>
            <label htmlFor="inicioEm">Data de início</label>
            <input
              id="inicioEm"
              type="datetime-local"
              value={formulario.inicioEm}
              onChange={(e) => alterar('inicioEm', e.target.value)}
              required
            />
          </div>

          <div>
            <label htmlFor="fimEm">Data de encerramento</label>
            <input
              id="fimEm"
              type="datetime-local"
              value={formulario.fimEm}
              onChange={(e) => alterar('fimEm', e.target.value)}
              required
            />
          </div>
        </div>

        <label htmlFor="status">Status</label>
        <select
          id="status"
          value={formulario.status}
          onChange={(e) => alterar('status', e.target.value)}
        >
          <option value="RASCUNHO">Rascunho</option>
          <option value="ATIVA">Ativa</option>
          <option value="ENCERRADA">Encerrada</option>
        </select>

        <label htmlFor="regulamentoUrl">Link do regulamento (opcional)</label>
        <input
          id="regulamentoUrl"
          type="url"
          value={formulario.regulamentoUrl}
          onChange={(e) => alterar('regulamentoUrl', e.target.value)}
          maxLength={500}
          placeholder="https://..."
        />

        {erro && <p className="campanha-mensagem erro">{erro}</p>}
        {sucesso && <p className="campanha-mensagem sucesso">{sucesso}</p>}

        <div className="campanha-acoes">
          <button type="submit" disabled={salvando}>
            {salvando ? 'Salvando...' : editando ? 'Salvar alterações' : 'Criar campanha'}
          </button>

          {editando && (
            <button type="button" className="campanha-cancelar" onClick={cancelarEdicao}>
              Cancelar
            </button>
          )}
        </div>
      </form>

      <div className="campanha-lista">
        <h2>Campanhas cadastradas</h2>

        {carregando ? (
          <p>Carregando campanhas...</p>
        ) : campanhas.length === 0 ? (
          <p>Nenhuma campanha cadastrada.</p>
        ) : (
          campanhas.map((campanha) => (
            <article className="campanha-item" key={campanha.id}>
              <div>
                <h3>{campanha.nome}</h3>
                <p>/{campanha.slug}</p>
                <span className={`campanha-status ${campanha.status.toLowerCase()}`}>
                  {campanha.status}
                </span>
              </div>

              <button type="button" onClick={() => prepararEdicao(campanha)}>
                Editar
              </button>
            </article>
          ))
        )}
      </div>
    </section>
  );
}
