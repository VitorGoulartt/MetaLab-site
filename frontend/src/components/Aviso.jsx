import Moldura from './Moldura.jsx';

export default function Aviso({ titulo, texto, children }) {
  return (
    <Moldura>
      <h1 className="titulo">
        <span className="destaque">{titulo}</span>
      </h1>
      <p className="intro">{texto}</p>
      {children}
    </Moldura>
  );
}
