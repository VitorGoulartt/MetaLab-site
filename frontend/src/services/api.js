export class ApiError extends Error {
  constructor(status, mensagem, campos = {}) {
    super(mensagem);
    this.status = status;
    this.campos = campos;
  }
}

const BASE = import.meta.env.VITE_API_URL || '';

function mensagemPadrao(status) {
  if (status === 401) return 'Sua sessão expirou. Entre de novo.';
  if (status === 403) return 'Você não tem permissão para isso.';
  if (status === 429) return 'Muitas tentativas. Aguarde um minuto e tente de novo.';
  if (status >= 500) return 'O servidor está instável. Tente de novo em instantes.';
  return 'Não foi possível concluir. Tente de novo.';
}

export async function chamar(caminho, opcoes = {}) {
  const controle = new AbortController();
  const limite = setTimeout(() => controle.abort(), 30000);
  let resposta;
  try {
    resposta = await fetch(`${BASE}${caminho}`, {
      ...opcoes,
      credentials: 'include', // envia o cookie de sessão do painel
      signal: controle.signal,
      headers: {
        'Content-Type': 'application/json',
        'X-Requested-With': 'metalab', // o backend exige este cabeçalho nas operações do painel
        ...opcoes.headers,
      },
    });
  } catch {
    throw new ApiError(0, 'Sem conexão com o servidor. Confira a internet e tente de novo.');
  } finally {
    clearTimeout(limite);
  }

  let corpo = null;
  try {
    corpo = await resposta.json();
  } catch {
    corpo = null; // respostas 204 não têm corpo
  }
  if (!resposta.ok) {
    throw new ApiError(resposta.status, corpo?.mensagem || mensagemPadrao(resposta.status), corpo?.campos || {});
  }
  return corpo;
}

// Público
export const buscarCampanha = (slug) => chamar(`/api/campanhas/${encodeURIComponent(slug)}`);

export const participar = (slug, dados) =>
  chamar(`/api/campanhas/${encodeURIComponent(slug)}/participacoes`, {
    method: 'POST',
    body: JSON.stringify(dados),
  });

// Painel: sessão
export const entrar = (email, senha) =>
  chamar('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, senha }) });
export const sair = () => chamar('/api/auth/logout', { method: 'POST' });
export const eu = () => chamar('/api/auth/me');
