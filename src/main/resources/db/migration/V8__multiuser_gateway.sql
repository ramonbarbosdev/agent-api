CREATE TABLE agent.usuario (
    id_usuario       UUID         PRIMARY KEY,
    ds_email         VARCHAR(255) NOT NULL,
    ds_password_hash VARCHAR(255) NOT NULL,
    dt_criacao       TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT usuario_email_uk UNIQUE (ds_email)
);

CREATE TABLE agent.ai_connection (
    id_connection          UUID         PRIMARY KEY,
    id_usuario             UUID         NOT NULL REFERENCES agent.usuario (id_usuario) ON DELETE CASCADE,
    tp_provider            VARCHAR(32)  NOT NULL,
    nm_label               VARCHAR(128),
    ds_api_key_ciphertext  TEXT         NOT NULL,
    fl_ativo               BOOLEAN      NOT NULL DEFAULT TRUE,
    dt_last_validated      TIMESTAMP,
    dt_criacao             TIMESTAMP    NOT NULL DEFAULT NOW(),
    dt_atualizacao         TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_ai_connection_usuario ON agent.ai_connection (id_usuario);

CREATE TABLE agent.projeto (
    cd_projeto   VARCHAR(64)   PRIMARY KEY,
    nm_nome      VARCHAR(255)  NOT NULL,
    ds_path      VARCHAR(1024) NOT NULL,
    fl_enabled   BOOLEAN       NOT NULL DEFAULT TRUE
);

ALTER TABLE agent.assistente
    ADD COLUMN id_usuario UUID REFERENCES agent.usuario (id_usuario) ON DELETE CASCADE,
    ADD COLUMN id_connection UUID REFERENCES agent.ai_connection (id_connection),
    ADD COLUMN cd_projeto VARCHAR(64);

ALTER TABLE agent.assistente DROP CONSTRAINT assistente_cd_assistente_uk;

CREATE UNIQUE INDEX assistente_usuario_codigo_uk
    ON agent.assistente (id_usuario, cd_assistente)
    WHERE id_usuario IS NOT NULL;

UPDATE agent.assistente SET fl_ativo = FALSE WHERE id_usuario IS NULL;

CREATE TABLE agent.gateway_session (
    id_session          UUID         PRIMARY KEY,
    id_usuario          UUID         NOT NULL REFERENCES agent.usuario (id_usuario) ON DELETE CASCADE,
    id_assistente       UUID         NOT NULL REFERENCES agent.assistente (id_assistente) ON DELETE CASCADE,
    id_connection       UUID         NOT NULL REFERENCES agent.ai_connection (id_connection),
    id_conversa         UUID UNIQUE REFERENCES agent.conversa (id_conversa) ON DELETE SET NULL,
    cd_cursor_agent_id  VARCHAR(255),
    tp_status           VARCHAR(32)  NOT NULL,
    dt_criacao          TIMESTAMP    NOT NULL DEFAULT NOW(),
    dt_atualizacao      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_gateway_session_usuario ON agent.gateway_session (id_usuario);
