import { Navigate, Outlet, useLocation } from 'react-router-dom';
import Moldura from '../components/Moldura.jsx';
import { useAuth } from '../contexts/AuthContext.jsx';
import SemPermissao from '../pages/painel/SemPermissao.jsx';

/** Exige login e, se `perfis` for informado, um desses perfis. */
export default function RotaProtegida({ perfis }) {
  const { usuario, carregando } = useAuth();
  const local = useLocation();

  if (carregando) {
    return (
      <Moldura ocupado>
        <p className="intro">Carregando…</p>
      </Moldura>
    );
  }
  if (!usuario) {
    return <Navigate to="/painel/entrar" replace state={{ de: local.pathname }} />;
  }
  if (perfis && !perfis.includes(usuario.perfil)) {
    return <SemPermissao />;
  }
  return <Outlet />;
}
