import { useEffect, useMemo, useState } from 'react';
import {
  listarCampanhasAdmin,
  listarParticipantes,
} from '../../services/api.js';
import './Participantes.css';

function formatarData(data) {
  if (!data) return '—';

  return new Date(data).toLocaleString('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  });
}

export default function Participantes() {
  const [participantes, setParticipantes] = useState([]);
  const [campanhas, setCampanhas] = useState([]);
  const [campanhaId, setCampanhaId] = useState('');
  const [pesquisa, setPesquisa] = useState('');
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    async function carregarCampanhas() {
      try {
        const dados = await listarCampanhasAdmin();
        setCampanhas(Array.isArray(dados) ? dados : []);
      } catch (error) {
        setErro(error.message || 'Não foi possível carregar as campanhas.');
      }
    }

    carregarCampanhas();
  }, []);

  useEffect(() => {
    let ativo = true;

    async function carregarParticipantes() {
      setCarregando(true);
      setErro('');

      try {
        const dados = await listarParticipantes(campanhaId || null);

        if (ativo) {
          setParticipantes(Array.isArray(dados) ? dados : []);
        }
      } catch (error) {
        if (ativo) {
          setErro(error.message || 'Não foi possível carregar os participantes.');
        }
      } finally {
        if (ativo) setCarregando(false);
      }
    }

    carregarParticipantes();

    return () => {
      ativo = false;
    };
  }, [campanhaId]);

  const participantesFiltrados = useMemo(() => {
    const termo = pesquisa.trim().toLowerCase();

    if (!termo) return participantes;

    return participantes.filter((p) =>
      [
        p.nome,
        p.email,
        p.telefone,
        p.campanhaNome,
      ].some((campo) => String(campo || '').toLowerCase().includes(termo))
    );
  }, [participantes, pesquisa]);

  return (
    <main className="participantes-pagina">
      <header className="participantes-cabecalho">
        <div>
          <h1 className="titulo-painel">Participantes</h1>
          <p className="intro">
            Consulte e pesquise as pessoas inscritas nas campanhas.
          </p>
        </div>

        <div className="participantes-total">
          <strong>{participantesFiltrados.length}</strong>
          <span>participantes</span>
        </div>
      </header>

      <section className="participantes-filtros">
        <input
          type="search"
          placeholder="Buscar por nome, e-mail ou telefone..."
          value={pesquisa}
          onChange={(e) => setPesquisa(e.target.value)}
          aria-label="Pesquisar participantes"
        />

        <select
          value={campanhaId}
          onChange={(e) => setCampanhaId(e.target.value)}
          aria-label="Filtrar por campanha"
        >
          <option value="">Todas as campanhas</option>
          {campanhas.map((campanha) => (
            <option key={campanha.id} value={campanha.id}>
              {campanha.nome}
            </option>
          ))}
        </select>
      </section>

      {erro && <div className="participantes-erro">{erro}</div>}

      <section className="participantes-tabela-container">
        {carregando ? (
          <p className="participantes-mensagem">Carregando participantes...</p>
        ) : participantesFiltrados.length === 0 ? (
          <p className="participantes-mensagem">
            Nenhum participante encontrado.
          </p>
        ) : (
          <div className="participantes-tabela-scroll">
            <table className="participantes-tabela">
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>E-mail</th>
                  <th>Telefone</th>
                  <th>CPF</th>
                  <th>Campanha</th>
                  <th>Inscrição</th>
                </tr>
              </thead>

              <tbody>
                {participantesFiltrados.map((p) => (
                  <tr key={p.id}>
                    <td data-label="Nome">{p.nome || '—'}</td>
                    <td data-label="E-mail">{p.email || '—'}</td>
                    <td data-label="Telefone">{p.telefone || '—'}</td>
                    <td data-label="CPF">{p.cpfMascarado || '—'}</td>
                    <td data-label="Campanha">{p.campanhaNome || '—'}</td>
                    <td data-label="Inscrição">{formatarData(p.criadoEm)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}
