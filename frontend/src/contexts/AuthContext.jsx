import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { eu, entrar as apiEntrar, sair as apiSair } from '../services/api.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [carregando, setCarregando] = useState(true);

  // Ao abrir o painel, pergunta ao servidor se ainda existe uma sessão válida.
  useEffect(() => {
    let ativo = true;
    eu()
      .then((u) => ativo && setUsuario(u))
      .catch(() => ativo && setUsuario(null))
      .finally(() => ativo && setCarregando(false));
    return () => {
      ativo = false;
    };
  }, []);

  const entrar = useCallback(async (email, senha) => {
    const u = await apiEntrar(email, senha);
    setUsuario(u);
    return u;
  }, []);

  const sair = useCallback(async () => {
    try {
      await apiSair();
    } catch {
      // se a sessão já expirou, o resultado é o mesmo: sair
    }
    setUsuario(null);
  }, []);

  const valor = useMemo(() => ({ usuario, carregando, entrar, sair }), [usuario, carregando, entrar, sair]);
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  return useContext(AuthContext);
}
