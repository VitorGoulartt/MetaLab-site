import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext.jsx';
import PainelLayout from './layouts/PainelLayout.jsx';
import Cadastro from './pages/Cadastro.jsx';
import Inicio from './pages/Inicio.jsx';
import NaoEncontrada from './pages/NaoEncontrada.jsx';
import EmBreve from './pages/painel/EmBreve.jsx';
import Entrar from './pages/painel/Entrar.jsx';
import Resumo from './pages/painel/Resumo.jsx';
import Privacidade from './pages/Privacidade.jsx';
import RotaProtegida from './routes/RotaProtegida.jsx';
import SorteioPublico from './pages/sorteio/SorteioPublico.jsx';

// A sessão só é consultada dentro de /painel: quem preenche o cadastro público não passa por aqui.
function AreaPainel() {
  return (
    <AuthProvider>
      <Outlet />
    </AuthProvider>
  );
}

export default function App() {
  return (
    <Routes>
      {/* Público */}
      <Route path="/" element={<Inicio />} />
      <Route path="/campanha/:slug" element={<Cadastro />} />
      <Route path="/privacidade" element={<Privacidade />} />
      <Route path="/sorteio/:slug" element={<SorteioPublico />} />

      {/* Painel */}
      <Route path="/login" element={<Navigate to="/painel/entrar" replace />} />
      <Route path="/painel" element={<AreaPainel />}>
        <Route path="entrar" element={<Entrar />} />
        <Route element={<RotaProtegida />}>
          <Route element={<PainelLayout />}>
            <Route index element={<Resumo />} />

            <Route element={<RotaProtegida perfis={['ADMIN']} />}>
              <Route path="campanhas" element={<EmBreve titulo="Campanhas" />} />
              <Route path="usuarios" element={<EmBreve titulo="Usuários" />} />
            </Route>
            <Route element={<RotaProtegida perfis={['ADMIN', 'SUPORTE']} />}>
              <Route path="participantes" element={<EmBreve titulo="Participantes" />} />
            </Route>
            <Route element={<RotaProtegida perfis={['ADMIN', 'OPERADOR']} />}>
              <Route path="sorteios" element={<EmBreve titulo="Sorteios" />} />
            </Route>
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<NaoEncontrada />} />
    </Routes>
  );
}
