-- Remove tabelas MVP em public (se existirem de versao anterior)
DROP TABLE IF EXISTS public.conversation_message CASCADE;
DROP TABLE IF EXISTS public.conversation CASCADE;

CREATE SCHEMA IF NOT EXISTS agent;

COMMENT ON SCHEMA agent IS
    'Schema do modulo Agent (assistentes LLM). Cadastros compartilhados de identidade permanecem no public quando integrados.';

CREATE TABLE agent.conversa (
    id_conversa    UUID         PRIMARY KEY,
    id_usuario     UUID,
    nm_assistente  VARCHAR(64)  NOT NULL,
    dt_criacao     TIMESTAMP    NOT NULL DEFAULT NOW(),
    dt_atualizacao TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE agent.mensagem_conversa (
    id_mensagemconversa UUID         PRIMARY KEY,
    id_conversa         UUID         NOT NULL REFERENCES agent.conversa (id_conversa) ON DELETE CASCADE,
    tp_papel            VARCHAR(16)  NOT NULL,
    ds_conteudo         TEXT         NOT NULL,
    dt_criacao          TIMESTAMP    NOT NULL DEFAULT NOW(),
    dt_atualizacao      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT mensagem_conversa_tp_papel_check CHECK (tp_papel IN ('user', 'assistant'))
);

CREATE INDEX ix_mensagem_conversa_conversa_criacao
    ON agent.mensagem_conversa (id_conversa, dt_criacao);

COMMENT ON TABLE agent.conversa IS
    'Thread de chat com assistente. Instalacao single-tenant (uma organizacao por deploy); sem id_organizacao.';

COMMENT ON TABLE agent.mensagem_conversa IS
    'Mensagens user/assistant de uma conversa. Historico enviado ao LLM e montado a partir desta tabela.';
