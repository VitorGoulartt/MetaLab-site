export const soDigitos = (valor) => (valor || '').replace(/\D/g, '');

export function mascararCpf(valor) {
  const d = soDigitos(valor).slice(0, 11);
  if (d.length <= 3) return d;
  if (d.length <= 6) return `${d.slice(0, 3)}.${d.slice(3)}`;
  if (d.length <= 9) return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6)}`;
  return `${d.slice(0, 3)}.${d.slice(3, 6)}.${d.slice(6, 9)}-${d.slice(9)}`;
}

export function mascararTelefone(valor) {
  const d = soDigitos(valor).slice(0, 11);
  if (d.length === 0) return '';
  if (d.length <= 2) return `(${d}`;
  const ddd = d.slice(0, 2);
  if (d.length <= 6) return `(${ddd}) ${d.slice(2)}`;
  const corte = d.length > 10 ? 7 : 6;
  return `(${ddd}) ${d.slice(2, corte)}-${d.slice(corte)}`;
}
