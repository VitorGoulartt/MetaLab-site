import { useEffect, useRef } from 'react';

const SRC = 'https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit';
let carregamento = null;

function carregarScript() {
  if (window.turnstile) return Promise.resolve();
  if (!carregamento) {
    carregamento = new Promise((ok, falha) => {
      const s = document.createElement('script');
      s.src = SRC;
      s.async = true;
      s.onload = ok;
      s.onerror = () => {
        carregamento = null;
        falha(new Error('Não foi possível carregar o Turnstile.'));
      };
      document.head.appendChild(s);
    });
  }
  return carregamento;
}

/**
 * Widget anti-robô. Cada token vale uma vez só e expira em 5 minutos:
 * depois de uma tentativa que falhou, aumente `resetSignal` para pedir um novo.
 */
export default function Turnstile({ siteKey, onToken, onErro, resetSignal = 0 }) {
  const caixa = useRef(null);
  const widget = useRef(null);

  useEffect(() => {
    let ativo = true;
    carregarScript()
      .then(() => {
        if (!ativo || !caixa.current) return;
        widget.current = window.turnstile.render(caixa.current, {
          sitekey: siteKey,
          theme: 'dark',
          size: 'flexible',
          language: 'pt-br',
          callback: (token) => onToken(token),
          'expired-callback': () => onToken(''),
          'error-callback': () => {
            onToken('');
            onErro?.();
          },
        });
      })
      .catch(() => onErro?.());
    return () => {
      ativo = false;
      if (widget.current !== null && window.turnstile) {
        window.turnstile.remove(widget.current);
        widget.current = null;
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [siteKey]);

  useEffect(() => {
    if (resetSignal > 0 && widget.current !== null && window.turnstile) {
      window.turnstile.reset(widget.current);
      onToken('');
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [resetSignal]);

  return <div ref={caixa} className="turnstile" />;
}
