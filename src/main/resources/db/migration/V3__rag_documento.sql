CREATE TABLE agent.documento (
    id_documento   UUID         PRIMARY KEY,
    nm_titulo      VARCHAR(255) NOT NULL,
    nm_fonte       VARCHAR(255) NOT NULL,
    dt_criacao     TIMESTAMP    NOT NULL DEFAULT NOW(),
    dt_atualizacao TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE agent.documento_chunk (
    id_documentochunk UUID         PRIMARY KEY,
    id_documento      UUID         NOT NULL REFERENCES agent.documento (id_documento) ON DELETE CASCADE,
    nu_ordem          INTEGER      NOT NULL,
    ds_conteudo       TEXT         NOT NULL,
    ds_busca          tsvector GENERATED ALWAYS AS (to_tsvector('portuguese', ds_conteudo)) STORED,
    dt_criacao        TIMESTAMP    NOT NULL DEFAULT NOW(),
    dt_atualizacao    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_documento_chunk_busca
    ON agent.documento_chunk USING GIN (ds_busca);

CREATE INDEX ix_documento_chunk_documento
    ON agent.documento_chunk (id_documento, nu_ordem);

COMMENT ON TABLE agent.documento IS
    'Documento de conhecimento indexado para RAG (single-tenant por instalacao).';

COMMENT ON TABLE agent.documento_chunk IS
    'Trechos do documento com busca full-text em portugues (ds_busca).';
