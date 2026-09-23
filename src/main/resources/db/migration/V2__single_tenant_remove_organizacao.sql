-- Ajuste para quem aplicou V1 anterior com id_organizacao (multi-tenant preparatorio)
DROP INDEX IF EXISTS agent.ix_conversa_organizacao;
ALTER TABLE agent.conversa DROP COLUMN IF EXISTS id_organizacao;
