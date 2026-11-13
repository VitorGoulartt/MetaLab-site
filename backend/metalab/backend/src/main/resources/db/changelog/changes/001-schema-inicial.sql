--liquibase formatted sql

--changeset metalab:001-clientes-e-campanhas
-- CPF nunca fica em texto puro: cpf_hash (HMAC) garante unicidade e busca,
-- cpf_cifrado (AES-GCM, feito pela aplicação) permite ao admin ver o número,
-- cpf_mascarado (ex.: ***.123.456-**) é o que suporte e operador enxergam.
CREATE TABLE clientes (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome_completo    VARCHAR(150) NOT NULL,
    email            VARCHAR(254) NOT NULL,
    telefone         VARCHAR(20)  NOT NULL,
    cpf_hash         CHAR(64)     NOT NULL,
    cpf_cifrado      TEXT         NOT NULL,
    cpf_mascarado    VARCHAR(14)  NOT NULL,
    origem_cadastro  VARCHAR(20)  NOT NULL DEFAULT 'QR_CODE',
    external_id      VARCHAR(100),
    criado_em        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_clientes_cpf_hash UNIQUE (cpf_hash),
    CONSTRAINT ck_clientes_origem CHECK (origem_cadastro IN ('QR_CODE', 'IMPORTACAO', 'ADMIN'))
);
CREATE INDEX ix_clientes_email ON clientes (lower(email));
CREATE INDEX ix_clientes_telefone ON clientes (telefone);
CREATE UNIQUE INDEX uq_clientes_external_id ON clientes (external_id) WHERE external_id IS NOT NULL;

CREATE TABLE campanhas (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    slug                VARCHAR(80)  NOT NULL,
    nome                VARCHAR(150) NOT NULL,
    descricao           TEXT,
    inicio_em           TIMESTAMPTZ  NOT NULL,
    fim_em              TIMESTAMPTZ  NOT NULL,
    status              VARCHAR(15)  NOT NULL DEFAULT 'RASCUNHO',
    regulamento_url     VARCHAR(500),
    versao_regulamento  VARCHAR(20)  NOT NULL DEFAULT '1',
    versao_privacidade  VARCHAR(20)  NOT NULL DEFAULT '1',
    criado_em           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_campanhas_slug UNIQUE (slug),
    CONSTRAINT ck_campanhas_slug CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
    CONSTRAINT ck_campanhas_status CHECK (status IN ('RASCUNHO', 'ATIVA', 'ENCERRADA')),
    CONSTRAINT ck_campanhas_periodo CHECK (fim_em > inicio_em)
);

--changeset metalab:002-participacoes-e-administradores
-- Um cadastro por CPF em cada campanha: como o cliente é único por CPF,
-- a restrição (cliente_id, campanha_id) garante isso no banco.
CREATE TABLE participacoes (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cliente_id          BIGINT      NOT NULL REFERENCES clientes (id),
    campanha_id         BIGINT      NOT NULL REFERENCES campanhas (id),
    faixa_etaria        VARCHAR(10) NOT NULL,
    responsavel_nome    VARCHAR(150),
    consentimento_em    TIMESTAMPTZ NOT NULL,
    versao_regulamento  VARCHAR(20) NOT NULL,
    versao_privacidade  VARCHAR(20) NOT NULL,
    criado_em           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_participacoes_cliente_campanha UNIQUE (cliente_id, campanha_id),
    CONSTRAINT uq_participacoes_id_campanha UNIQUE (id, campanha_id),
    CONSTRAINT ck_participacoes_faixa CHECK (faixa_etaria IN ('ADULTO', 'MENOR')),
    CONSTRAINT ck_participacoes_responsavel CHECK (faixa_etaria = 'ADULTO' OR responsavel_nome IS NOT NULL)
);
CREATE INDEX ix_participacoes_campanha ON participacoes (campanha_id);

CREATE TABLE administradores (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome             VARCHAR(150) NOT NULL,
    email            VARCHAR(254) NOT NULL,
    senha_hash       VARCHAR(100) NOT NULL,
    perfil           VARCHAR(10)  NOT NULL,
    ativo            BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    ultimo_login_em  TIMESTAMPTZ,
    CONSTRAINT ck_administradores_perfil CHECK (perfil IN ('ADMIN', 'OPERADOR', 'SUPORTE'))
);
CREATE UNIQUE INDEX uq_administradores_email ON administradores (lower(email));

--changeset metalab:003-sorteios-e-ganhadores
-- elegiveis_snapshot guarda os ids das participações elegíveis no momento do
-- sorteio, para o resultado poder ser auditado depois.
CREATE TABLE sorteios (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    campanha_id            BIGINT       NOT NULL REFERENCES campanhas (id),
    numero                 INT          NOT NULL,
    premio                 VARCHAR(150) NOT NULL,
    quantidade_ganhadores  INT          NOT NULL DEFAULT 1,
    status                 VARCHAR(12)  NOT NULL DEFAULT 'PENDENTE',
    realizado_em           TIMESTAMPTZ,
    realizado_por          BIGINT REFERENCES administradores (id),
    elegiveis_snapshot     JSONB,
    criado_em              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_sorteios_campanha_numero UNIQUE (campanha_id, numero),
    CONSTRAINT uq_sorteios_id_campanha UNIQUE (id, campanha_id),
    CONSTRAINT ck_sorteios_numero CHECK (numero > 0),
    CONSTRAINT ck_sorteios_quantidade CHECK (quantidade_ganhadores > 0),
    CONSTRAINT ck_sorteios_status CHECK (status IN ('PENDENTE', 'REALIZADO', 'CANCELADO'))
);

-- Quem ganha não concorre aos sorteios seguintes da mesma campanha:
-- a restrição (campanha_id, participacao_id) impede ganhar duas vezes, e as
-- chaves compostas garantem que sorteio e participação são da mesma campanha.
CREATE TABLE ganhadores (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sorteio_id      BIGINT      NOT NULL,
    campanha_id     BIGINT      NOT NULL,
    participacao_id BIGINT      NOT NULL,
    posicao         INT         NOT NULL DEFAULT 1,
    criado_em       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_ganhadores_sorteio FOREIGN KEY (sorteio_id, campanha_id)
        REFERENCES sorteios (id, campanha_id),
    CONSTRAINT fk_ganhadores_participacao FOREIGN KEY (participacao_id, campanha_id)
        REFERENCES participacoes (id, campanha_id),
    CONSTRAINT uq_ganhadores_campanha_participacao UNIQUE (campanha_id, participacao_id),
    CONSTRAINT uq_ganhadores_sorteio_posicao UNIQUE (sorteio_id, posicao),
    CONSTRAINT ck_ganhadores_posicao CHECK (posicao > 0)
);

--changeset metalab:004-webhooks-e-auditoria
CREATE TABLE webhooks (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome             VARCHAR(100) NOT NULL,
    url              VARCHAR(500) NOT NULL,
    segredo_cifrado  TEXT         NOT NULL,
    eventos          VARCHAR(300) NOT NULL,
    ativo            BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em        TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_webhooks_https CHECK (url LIKE 'https://%')
);

-- Também funciona como fila de envio: o cadastro grava o evento aqui como
-- PENDENTE e um processo separado envia, com reenvio em intervalos crescentes.
CREATE TABLE webhook_logs (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    webhook_id         BIGINT      NOT NULL REFERENCES webhooks (id),
    evento             VARCHAR(50) NOT NULL,
    event_id           UUID        NOT NULL,
    payload            JSONB       NOT NULL,
    status             VARCHAR(10) NOT NULL DEFAULT 'PENDENTE',
    tentativas         INT         NOT NULL DEFAULT 0,
    proxima_tentativa  TIMESTAMPTZ NOT NULL DEFAULT now(),
    http_status        INT,
    erro               TEXT,
    criado_em          TIMESTAMPTZ NOT NULL DEFAULT now(),
    enviado_em         TIMESTAMPTZ,
    CONSTRAINT uq_webhook_logs_evento UNIQUE (webhook_id, event_id),
    CONSTRAINT ck_webhook_logs_status CHECK (status IN ('PENDENTE', 'ENVIADO', 'FALHA'))
);
CREATE INDEX ix_webhook_logs_fila ON webhook_logs (status, proxima_tentativa);

CREATE TABLE audit_logs (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    administrador_id  BIGINT REFERENCES administradores (id),
    acao              VARCHAR(60) NOT NULL,
    entidade          VARCHAR(60) NOT NULL,
    entidade_id       VARCHAR(60),
    detalhes          JSONB,
    ip                VARCHAR(45),
    criado_em         TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_audit_logs_criado_em ON audit_logs (criado_em);

--changeset metalab:005-rls
-- O navegador não deve falar com o banco: com RLS ligado e sem políticas, a API
-- automática do Supabase (chaves anon e authenticated) não lê nada. O backend
-- conecta com o usuário dono das tabelas e continua com acesso normal.
ALTER TABLE clientes              ENABLE ROW LEVEL SECURITY;
ALTER TABLE campanhas             ENABLE ROW LEVEL SECURITY;
ALTER TABLE participacoes         ENABLE ROW LEVEL SECURITY;
ALTER TABLE administradores       ENABLE ROW LEVEL SECURITY;
ALTER TABLE sorteios              ENABLE ROW LEVEL SECURITY;
ALTER TABLE ganhadores            ENABLE ROW LEVEL SECURITY;
ALTER TABLE webhooks              ENABLE ROW LEVEL SECURITY;
ALTER TABLE webhook_logs          ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs            ENABLE ROW LEVEL SECURITY;
ALTER TABLE databasechangelog     ENABLE ROW LEVEL SECURITY;
ALTER TABLE databasechangeloglock ENABLE ROW LEVEL SECURITY;