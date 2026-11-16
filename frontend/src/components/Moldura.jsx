import Cabecalho from './Cabecalho.jsx';

export default function Moldura({ children, ocupado = false }) {
  return (
    <main className="pagina" aria-busy={ocupado}>
      <Cabecalho />
      {children}
    </main>
  );
}
