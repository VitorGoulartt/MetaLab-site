import { useEffect, useState } from 'react';
import {
  listarUsuarios,
  criarUsuario,
  editarUsuario,
  alterarStatusUsuario,
} from '../../services/api.js';
import './Usuarios.css';

const formularioInicial = {
  nome: '',
  email: '',
  senha: '',
  perfil: 'OPERADOR',
};

export default function Usuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [formulario, setFormulario] = useState(formularioInicial);
  const [editando, setEditando] = useState(null);
  const [modalAberto, setModalAberto] = useState(false);
  const [carregando, setCarregando] = useState(true);
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState('');
  const [sucesso, setSucesso] = useState('');

  async function carregarUsuarios() {
    setCarregando(true);
    setErro('');

    try {
      const dados = await listarUsuarios();
      setUsuarios(Array.isArray(dados) ? dados : []);
    } catch (error) {
      setErro(error.message || 'Não foi possível carregar os usuários.');
    } finally {
      setCarregando(false);
    }
  }

  useEffect(() => {
    carregarUsuarios();
  }, []);

  function abrirNovo() {
    setEditando(null);
    setFormulario(formularioInicial);
    setErro('');
    setModalAberto(true);
  }

  function abrirEdicao(usuario) {
    setEditando(usuario);
    setFormulario({
      nome: usuario.nome || '',
      email: usuario.email || '',
      senha: '',
      perfil: usuario.perfil || 'OPERADOR',
    });
    setErro('');
    setModalAberto(true);
  }

  function atualizarCampo(event) {
    const { name, value } = event.target;
    setFormulario((atual) => ({ ...atual, [name]: value }));
  }

  async function salvar(event) {
    event.preventDefault();
    setSalvando(true);
    setErro('');
    setSucesso('');

    try {
      if (editando) {
        const dados = {
          nome: formulario.nome,
          email: formulario.email,
          perfil: formulario.perfil,
        };

        await editarUsuario(editando.id, dados);
        setSucesso('Usuário atualizado com sucesso.');
      } else {
        await criarUsuario(formulario);
        setSucesso('Usuário criado com sucesso.');
      }

      setModalAberto(false);
      await carregarUsuarios();
    } catch (error) {
      setErro(error.message || 'Não foi possível salvar o usuário.');
    } finally {
      setSalvando(false);
    }
  }

  async function alternarStatus(usuario) {
    const acao = usuario.ativo ? 'desativar' : 'ativar';

    if (!window.confirm(`Deseja ${acao} o usuário ${usuario.nome}?`)) {
      return;
    }

    setErro('');
    setSucesso('');

    try {
      await alterarStatusUsuario(usuario.id, !usuario.ativo);
      setSucesso(`Usuário ${usuario.ativo ? 'desativado' : 'ativado'} com sucesso.`);
      await carregarUsuarios();
    } catch (error) {
      setErro(error.message || 'Não foi possível alterar o status.');
    }
  }

  return (
    <main className="usuarios-pagina">
      <header className="usuarios-cabecalho">
        <div>
          <h1 className="titulo-painel">Usuários</h1>
          <p className="intro">
            Gerencie os acessos e as permissões da equipe.
          </p>
        </div>

        <button className="usuarios-botao-principal" onClick={abrirNovo}>
          + Novo usuário
        </button>
      </header>

      {erro && <div className="usuarios-alerta erro">{erro}</div>}
      {sucesso && <div className="usuarios-alerta sucesso">{sucesso}</div>}

      <section className="usuarios-tabela-container">
        {carregando ? (
          <p className="usuarios-mensagem">Carregando usuários...</p>
        ) : usuarios.length === 0 ? (
          <p className="usuarios-mensagem">Nenhum usuário cadastrado.</p>
        ) : (
          <div className="usuarios-tabela-scroll">
            <table className="usuarios-tabela">
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>E-mail</th>
                  <th>Perfil</th>
                  <th>Status</th>
                  <th>Último acesso</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {usuarios.map((usuario) => (
                  <tr key={usuario.id}>
                    <td>{usuario.nome}</td>
                    <td>{usuario.email}</td>
                    <td>{usuario.perfil}</td>
                    <td>
                      <span className={`usuarios-status ${usuario.ativo ? 'ativo' : 'inativo'}`}>
                        {usuario.ativo ? 'Ativo' : 'Inativo'}
                      </span>
                    </td>
                    <td>
                      {usuario.ultimoLoginEm
                        ? new Date(usuario.ultimoLoginEm).toLocaleString('pt-BR')
                        : 'Nunca acessou'}
                    </td>
                    <td>
                      <div className="usuarios-acoes">
                        <button onClick={() => abrirEdicao(usuario)}>
                          Editar
                        </button>
                        <button
                          className={usuario.ativo ? 'desativar' : 'ativar'}
                          onClick={() => alternarStatus(usuario)}
                        >
                          {usuario.ativo ? 'Desativar' : 'Ativar'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {modalAberto && (
        <div className="usuarios-modal-fundo" onMouseDown={(e) => {
          if (e.target === e.currentTarget) setModalAberto(false);
        }}>
          <section className="usuarios-modal">
            <header>
              <h2>{editando ? 'Editar usuário' : 'Novo usuário'}</h2>
              <button type="button" onClick={() => setModalAberto(false)}>
                ×
              </button>
            </header>

            <form onSubmit={salvar}>
              <label>
                Nome completo
                <input
                  name="nome"
                  value={formulario.nome}
                  onChange={atualizarCampo}
                  required
                />
              </label>

              <label>
                E-mail
                <input
                  name="email"
                  type="email"
                  value={formulario.email}
                  onChange={atualizarCampo}
                  required
                />
              </label>

              {!editando && (
                <label>
                  Senha
                  <input
                    name="senha"
                    type="password"
                    value={formulario.senha}
                    onChange={atualizarCampo}
                    minLength={10}
                    maxLength={72}
                    required
                  />
                  <small>A senha deve ter entre 10 e 72 caracteres.</small>
                </label>
              )}

              <label>
                Perfil de acesso
                <select
                  name="perfil"
                  value={formulario.perfil}
                  onChange={atualizarCampo}
                  required
                >
                  <option value="ADMIN">Administrador</option>
                  <option value="OPERADOR">Operador</option>
                  <option value="SUPORTE">Suporte</option>
                </select>
              </label>

              {erro && <div className="usuarios-alerta erro">{erro}</div>}

              <footer>
                <button
                  type="button"
                  className="usuarios-botao-secundario"
                  onClick={() => setModalAberto(false)}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="usuarios-botao-principal"
                  disabled={salvando}
                >
                  {salvando ? 'Salvando...' : 'Salvar usuário'}
                </button>
              </footer>
            </form>
          </section>
        </div>
      )}
    </main>
  );
}
