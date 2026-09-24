ALTER TABLE agent.documento
    ADD COLUMN ds_conteudo TEXT;

UPDATE agent.documento d
SET ds_conteudo = COALESCE(
    (SELECT string_agg(c.ds_conteudo, E'\n' ORDER BY c.nu_ordem)
     FROM agent.documento_chunk c
     WHERE c.id_documento = d.id_documento),
    ''
);

ALTER TABLE agent.documento
    ALTER COLUMN ds_conteudo SET NOT NULL;

COMMENT ON COLUMN agent.documento.ds_conteudo IS
    'Texto integral do documento para edição; trechos em documento_chunk são derivados na indexação.';
