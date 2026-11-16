import { cpfValido } from './cpf.js';
import { soDigitos } from './mascaras.js';

const doisNomes = (texto) => texto.trim().split(/\s+/).filter(Boolean).length >= 2;

// Ordem em que os campos aparecem na tela (o primeiro com erro recebe o foco).
export const ORDEM = ['nome', 'email', 'telefone', 'cpf', 'faixaEtaria', 'responsavelNome', 'aceite', 'captcha'];

export function validar(v, token) {
  const e = {};
  if (!doisNomes(v.nome)) e.nome = 'Informe nome e sobrenome.';
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(v.email.trim())) {
    e.email = 'Digite um e-mail válido, como nome@exemplo.com.';
  }
  const tel = soDigitos(v.telefone).length;
  if (tel !== 10 && tel !== 11) e.telefone = 'Digite o telefone com DDD.';
  if (!cpfValido(v.cpf)) e.cpf = 'Digite um CPF válido, com 11 números.';
  if (!v.faixaEtaria) e.faixaEtaria = 'Escolha uma das opções.';
  if (v.faixaEtaria === 'MENOR' && !doisNomes(v.responsavelNome)) {
    e.responsavelNome = 'Informe nome e sobrenome do responsável.';
  }
  if (!v.aceite) e.aceite = 'Marque a caixa para confirmar a participação.';
  if (!token) e.captcha = 'Confirme que você não é um robô.';
  return e;
}

export const validarCampo = (campo, valores) => validar(valores, 'ok')[campo];
