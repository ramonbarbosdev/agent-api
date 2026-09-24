CREATE TABLE agent.assistente (
    id_assistente     UUID         PRIMARY KEY,
    cd_assistente     VARCHAR(64)  NOT NULL,
    nm_nome           VARCHAR(255) NOT NULL,
    ds_descricao      VARCHAR(512),
    ds_system_prompt  TEXT         NOT NULL,
    nm_modelo         VARCHAR(128),
    fl_ativo          BOOLEAN      NOT NULL DEFAULT TRUE,
    fl_rag_inject     BOOLEAN      NOT NULL DEFAULT TRUE,
    nu_rag_top_k      INTEGER      NOT NULL DEFAULT 4,
    dt_criacao        TIMESTAMP    NOT NULL DEFAULT NOW(),
    dt_atualizacao    TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT assistente_cd_assistente_uk UNIQUE (cd_assistente)
);

CREATE TABLE agent.assistente_tool (
    id_assistentetool UUID        PRIMARY KEY,
    id_assistente     UUID        NOT NULL REFERENCES agent.assistente (id_assistente) ON DELETE CASCADE,
    nm_tool           VARCHAR(128) NOT NULL,
    dt_criacao        TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT assistente_tool_uk UNIQUE (id_assistente, nm_tool)
);

CREATE INDEX ix_assistente_tool_assistente ON agent.assistente_tool (id_assistente);

COMMENT ON TABLE agent.assistente IS
    'Assistentes configuráveis (prompt, modelo, RAG). cd_assistente é o ID usado na API de chat.';

COMMENT ON TABLE agent.assistente_tool IS
    'Tools permitidas por assistente (nomes do ToolRegistry).';
