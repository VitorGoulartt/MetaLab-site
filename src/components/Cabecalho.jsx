import logo from '../assets/marca/logo-horizontal.svg';

export default function Cabecalho() {
  return (
    <header className="cabecalho">
      <img className="logo" src={logo} alt="Metalab" />
    </header>
  );
}
