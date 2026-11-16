import { Link } from 'react-router-dom';

export default function SemPermissao() {
  return (
    <>
      <h1 className="titulo-painel">Sem permissão</h1>
      <p className="intro">O seu perfil não tem acesso a esta tela.</p>
      <Link to="/painel" className="link-voltar">
        Voltar ao resumo
      </Link>
    </>
  );
}
