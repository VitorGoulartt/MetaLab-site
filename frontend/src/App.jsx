
import { Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext.jsx';
import PainelLayout from './layouts/PainelLayout.jsx';
import Cadastro from './pages/Cadastro.jsx';
import Inicio from './pages/Inicio.jsx';
import NaoEncontrada from './pages/NaoEncontrada.jsx';
import Campanhas from './pages/painel/Campanhas.jsx';
import Entrar from './pages/painel/Entrar.jsx';
import Resumo from './pages/painel/Resumo.jsx';
import Privacidade from './pages/Privacidade.jsx';
import RotaProtegida from './routes/RotaProtegida.jsx';
import SorteioPublico from './pages/sorteio/SorteioPublico.jsx';
import Sorteios from './pages/painel/Sorteios.jsx';
import Participantes from './pages/painel/Participantes.jsx';
import Usuarios from './pages/painel/Usuarios.jsx';


function AreaPainel() {
  return (
    <AuthProvider>
      <Outlet />
    </AuthProvider>
  );
}

// Página temporária para funcionalidades ainda não implementadas.
function EmBreve({ titulo }) {
  return (
    <div style={{ padding: '32px' }}>
      <h1>{titulo}</h1>
      <p>Esta funcionalidade está em desenvolvimento.</p>
    </div>
  );
}

export default function App() {
  return (
    <Routes>
      {/* Público */}
      <Route path="/" element={<Inicio />} />
      <Route path="/campanha/:slug" element={<Cadastro />} />
      <Route path="/campanha/:slug/participacoes" element={<SorteioPublico />} />
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
              <Route path="campanhas" element={<Campanhas />} />
              <Route path="usuarios" element={<Usuarios />} />
            </Route>

            <Route element={<RotaProtegida perfis={['ADMIN', 'OPERADOR', 'SUPORTE']} />}>
              <Route path="participantes" element={<Participantes />} />
            </Route>

            <Route element={<RotaProtegida perfis={['ADMIN', 'OPERADOR']} />}>
              <Route path="sorteios" element={<Sorteios />} />
            </Route>
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<NaoEncontrada />} />
    </Routes>
  );
}
