export const ROTULO_PERFIL = {
  ADMIN: 'Administrador',
  OPERADOR: 'Operador',
  SUPORTE: 'Suporte',
};

// As rotas em App.jsx devem usar os mesmos perfis daqui.
export const MENU = [
  {
    caminho: '/painel',
    rotulo: 'Resumo',
    fim: true,
    perfis: ['ADMIN', 'OPERADOR', 'SUPORTE'],
  },
  {
    caminho: '/painel/campanhas',
    rotulo: 'Campanhas',
    descricao: 'Criar campanhas, abrir e encerrar, gerar o QR Code.',
    perfis: ['ADMIN'],
  },
  {
    caminho: '/painel/participantes',
    rotulo: 'Participantes',
    descricao: 'Consultar quem já está participando de cada campanha.',
    perfis: ['ADMIN', 'OPERADOR', 'SUPORTE'],
  },
  {
    caminho: '/painel/sorteios',
    rotulo: 'Sorteios',
    descricao: 'Executar sorteios e visualizar os ganhadores.',
    perfis: ['ADMIN', 'OPERADOR'],
  },
  {
    caminho: '/painel/usuarios',
    rotulo: 'Usuários',
    descricao: 'Cadastrar operadores e suporte.',
    perfis: ['ADMIN'],
  },
];
