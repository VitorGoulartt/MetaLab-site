import Moldura from '../components/Moldura.jsx';

export default function Inicio() {
  return (
    <Moldura>
      <h1 className="titulo">
        <span className="destaque">Escaneie o QR Code</span>
      </h1>
      <p className="intro">Aponte a câmera do celular para o QR Code da campanha para participar.</p>
    </Moldura>
  );
}
