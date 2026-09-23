-- Seed opcional para desenvolvimento (politica resumida de horas extras)
INSERT INTO agent.documento (id_documento, nm_titulo, nm_fonte, dt_criacao, dt_atualizacao)
VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'Politica interna de horas extras',
    'RH / Politica HE v1',
    NOW(),
    NOW()
)
ON CONFLICT (id_documento) DO NOTHING;

INSERT INTO agent.documento_chunk (id_documentochunk, id_documento, nu_ordem, ds_conteudo, dt_criacao, dt_atualizacao)
VALUES
    (
        'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        1,
        'Toda hora extra deve ser previamente autorizada pelo gestor imediato. Lancamentos sem aprovacao nao sao validos para pagamento ou compensacao.',
        NOW(),
        NOW()
    ),
    (
        'c3d4e5f6-a7b8-9012-cdef-123456789012',
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        2,
        'O limite mensal de horas extras segue o acordo coletivo e a legislacao vigente. Consulte o RH para o teto aplicavel ao seu contrato.',
        NOW(),
        NOW()
    ),
    (
        'd4e5f6a7-b8c9-0123-def0-234567890123',
        'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        3,
        'A compensacao em folga depende de acordo entre colaborador e gestor e deve ser registrada no sistema de ponto.',
        NOW(),
        NOW()
    )
ON CONFLICT (id_documentochunk) DO NOTHING;
