CREATE TABLE agent.tarefa_trabalho (
    id_tarefa       UUID         PRIMARY KEY,
    id_conversa     UUID         NOT NULL REFERENCES agent.conversa (id_conversa) ON DELETE CASCADE,
    nm_titulo       VARCHAR(200) NOT NULL,
    ds_descricao    TEXT,
    tp_prioridade   VARCHAR(16)  NOT NULL DEFAULT 'MEDIA',
    tp_status       VARCHAR(16)  NOT NULL DEFAULT 'PENDENTE',
    dt_prazo        DATE,
    dt_criacao      TIMESTAMP    NOT NULL DEFAULT NOW(),
    dt_atualizacao  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT tarefa_trabalho_tp_prioridade_check
        CHECK (tp_prioridade IN ('BAIXA', 'MEDIA', 'ALTA')),
    CONSTRAINT tarefa_trabalho_tp_status_check
        CHECK (tp_status IN ('PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDA', 'CANCELADA'))
);

CREATE INDEX ix_tarefa_trabalho_conversa_status
    ON agent.tarefa_trabalho (id_conversa, tp_status);

COMMENT ON TABLE agent.tarefa_trabalho IS
    'Tarefas de trabalho vinculadas a uma conversa (lista pessoal do thread de chat).';
