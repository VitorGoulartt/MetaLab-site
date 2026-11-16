import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Campo from '../../components/Campo.jsx';
import Moldura from '../../components/Moldura.jsx';
import { useAuth } from '../../contexts/AuthContext.jsx';
import { ApiError } from '../../services/api.js';

export default function Entrar() {
  const { usuario, carregando, entrar } = useAuth();
  const navegar = useNavigate();
  const local = useLocation();
  const destino = local.state?.de || '/painel';

  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState('');
  const [enviando, setEnviando] = useState(false);

  // Quem já tem sessão vai direto para o painel.
  useEffect(() => {
    if (!carregando && usuario) navegar(destino, { replace: true });
  }, [carregando, usuario, destino, navegar]);

  async function enviar(evento) {
    evento.preventDefault();
    setErro('');
    if (!email.trim() || !senha) {
      setErro('Informe o e-mail e a senha.');
      return;
    }
    setEnviando(true);
    try {
      await entrar(email.trim(), senha);
      navegar(destino, { replace: true });
    } catch (e) {
      setErro(e instanceof ApiError ? e.message : 'Algo deu errado. Tente de novo.');
      setSenha('');
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Moldura>
      <h1 className="titulo">
        <span className="destaque">Entrar no painel</span>
      </h1>
      <p className="intro">Use o e-mail e a senha que a Metalab cadastrou para você.</p>

      <form className="formulario" onSubmit={enviar} noValidate>
        <Campo
          id="email"
          rotulo="E-mail"
          type="email"
          autoComplete="username"
          inputMode="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <Campo
          id="senha"
          rotulo="Senha"
          type="password"
          autoComplete="current-password"
          value={senha}
          onChange={(e) => setSenha(e.target.value)}
        />
        {erro && (
          <p className="aviso-erro" role="alert">
            {erro}
          </p>
        )}
        <button type="submit" className="botao" disabled={enviando}>
          {enviando ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
    </Moldura>
  );
}
