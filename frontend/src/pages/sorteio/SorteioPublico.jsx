
import { useEffect, useState } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
import './SorteioPublico.css';

// Dados fictícios: usados somente para visualizar a animação.
const NOMES_DEMO = [
  'Ana Clara', 'Bruno Santos', 'Camila Oliveira',
  'Daniel Costa', 'Eduarda Lima', 'Felipe Martins',
  'Gabriela Souza', 'Henrique Alves', 'Isabela Rocha',
  'João Pedro', 'Larissa Mendes', 'Lucas Ferreira',
];

export default function SorteioPublico() {
  const { slug } = useParams();
  const [searchParams] = useSearchParams();
  const modoDemo = searchParams.get('demo') === '1';

  const [sorteando, setSorteando] = useState(false);
  const [ganhador, setGanhador] = useState(null);

  useEffect(() => {
    if (!sorteando) return;

    const temporizador = setTimeout(() => {
      const indice = Math.floor(Math.random() * NOMES_DEMO.length);
      setGanhador(NOMES_DEMO[indice]);
      setSorteando(false);
    }, 6500);

    return () => clearTimeout(temporizador);
  }, [sorteando]);

  function iniciarDemo() {
    setGanhador(null);
    setSorteando(true);
  }

  return (
    <main className="tela-sorteio">
      <header className="sorteio-cabecalho">
        <img
          src="/src/assets/marca/logo-horizontal.svg"
          alt="Metalab"
        />
        <span>APRESENTAÇÃO DO SORTEIO</span>
      </header>

      <section className="sorteio-conteudo">
        <p className="sorteio-identificacao">CAMPANHA</p>
        <h1>{slug.replaceAll('-', ' ')}</h1>

        <p className="sorteio-instrucao">
          {ganhador
            ? 'Temos um ganhador!'
            : sorteando
              ? 'Boa sorte a todos!'
              : 'O sorteio começará em instantes'}
        </p>

        <div className={`sorteio-visor ${sorteando ? 'animando' : ''}`}>
          {ganhador ? (
            <div className="sorteio-ganhador">
              <span>🏆 GANHADOR</span>
              <strong>{ganhador}</strong>
              <p>Parabéns!</p>
            </div>
          ) : sorteando ? (
            <div className="sorteio-lista">
              {[...NOMES_DEMO, ...NOMES_DEMO, ...NOMES_DEMO].map(
                (nome, indice) => (
                  <div className="sorteio-nome" key={indice}>
                    {nome}
                  </div>
                ),
              )}
            </div>
          ) : (
            <div className="sorteio-espera">
              <span>🎁</span>
              <strong>Aguardando o sorteio</strong>
            </div>
          )}
        </div>

        {modoDemo && (
          <button
            className="sorteio-botao-demo"
            onClick={iniciarDemo}
            disabled={sorteando}
          >
            {sorteando ? 'Sorteando...' : 'Simular sorteio'}
          </button>
        )}
      </section>

      <footer className="sorteio-rodape">
        METALAB • BOA SORTE!
      </footer>
    </main>
  );
}
