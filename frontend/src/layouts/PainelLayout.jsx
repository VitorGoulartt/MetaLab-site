import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import logo from '../assets/marca/logo-horizontal.svg';
import { MENU, ROTULO_PERFIL } from '../config/menu.js';
import { useAuth } from '../contexts/AuthContext.jsx';
import '../styles/painel.css';

export default function PainelLayout() {
  const { usuario, sair } = useAuth();
  const navegar = useNavigate();
  const itens = MENU.filter((item) => item.perfis.includes(usuario.perfil));

  async function aoSair() {
    await sair();
    navegar('/painel/entrar', { replace: true });
  }

  return (
    <div className="painel">
      <header className="painel-topo">
        <img className="logo" src={logo} alt="Metalab" />
        <div className="painel-usuario">
          <span className="painel-nome">{usuario.nome}</span>
          <small>{ROTULO_PERFIL[usuario.perfil]}</small>
        </div>
        <button type="button" className="botao-sair" onClick={aoSair}>
          Sair
        </button>
      </header>

      <nav className="painel-menu" aria-label="Menu do painel">
        {itens.map((item) => (
          <NavLink key={item.caminho} to={item.caminho} end={item.fim}>
            {item.rotulo}
          </NavLink>
        ))}
      </nav>

      <main className="painel-conteudo">
        <Outlet />
      </main>
    </div>
  );
}
