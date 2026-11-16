import { Link } from 'react-router-dom';
import { MENU } from '../../config/menu.js';
import { useAuth } from '../../contexts/AuthContext.jsx';

export default function Resumo() {
  const { usuario } = useAuth();
  const primeiroNome = usuario.nome.trim().split(/\s+/)[0];
  const atalhos = MENU.filter((i) => i.descricao && i.perfis.includes(usuario.perfil));

  return (
    <>
      <h1 className="titulo-painel">Olá, {primeiroNome}</h1>
      <p className="intro">O que você quer fazer agora?</p>
      <div className="grade-cartoes">
        {atalhos.map((item) => (
          <Link key={item.caminho} to={item.caminho} className="cartao-painel">
            <strong>{item.rotulo}</strong>
            <span>{item.descricao}</span>
          </Link>
        ))}
      </div>
    </>
  );
}
