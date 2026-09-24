# Cenário: assistente Benefícios RH (`BENEFICIOS_RH`)

Exemplo de **novo assistente** usando só o que já existe na plataforma (sem nova classe Java).
Reutiliza: `search_knowledge_base`, `obter_data_hora_servidor`.

## 1. Subir ambiente

- API + Postgres + Ollama (`ready` no diagnóstico).
- Front: **Console de agentes** → **Assistentes**.

## 2. Criar o assistente (UI)

| Campo | Valor |
|-------|--------|
| Código | `BENEFICIOS_RH` |
| Nome | Benefícios RH |
| Descrição | Dúvidas sobre plano de saúde, VR/VA e regras de benefícios |
| Prefixar prompt base | Sim |
| Modelo | (vazio ou `qwen3:8b`) |
| Ativo | Sim |
| Injetar RAG | Sim |
| RAG top-K | 4 |
| Tools | `search_knowledge_base`, `obter_data_hora_servidor` |

### Texto do prompt (domínio)

Cole no campo **Prompt de sistema**:

```text
Você é o assistente de benefícios do RH.

Responda apenas sobre benefícios corporativos (plano de saúde, vale refeição/alimentação,
convênios e elegibilidade).

Use search_knowledge_base quando a pergunta depender de política ou manual.
Use obter_data_hora_servidor só quando precisar de data/hora oficial para prazos.

Não invente valores, percentuais ou prazos. Se a base não tiver a informação, diga claramente.
Cite a fonte quando usar trechos recuperados (título/fonte do documento).

Nunca confirme alteração cadastral; você só orienta com base na documentação.
```

Salvar → **Criar assistente**.

## 3. Indexar a base de conhecimento

**Base de conhecimento** → Ingerir documento:

| Campo | Valor |
|-------|--------|
| Título | Manual de Benefícios 2026 |
| Fonte | RH / Benefícios |
| Conteúdo | (texto abaixo) |

```text
MANUAL DE BENEFÍCIOS — VIGÊNCIA 2026

Plano de saúde: elegível após 90 dias de contrato CLT. Dependentes: cônjuge e filhos até 21 anos
(com documentação na admissão do dependente).

Vale refeição: cartão creditado no 1º dia útil do mês. Valor diário conforme tabela interna
(divulgada no portal do colaborador).

Vale alimentação: não acumula saldo entre meses.

Alteração de plano: janela de troca em março e setembro; solicitação pelo portal RH até o dia 15.

Dúvidas não previstas neste manual devem ser encaminhadas ao e-mail beneficios@empresa.local.
```

Ou via API:

```bash
curl -s -X POST http://localhost:8080/api/agent/rag/documents \
  -H "Content-Type: application/json" \
  -d "{\"titulo\":\"Manual de Benefícios 2026\",\"fonte\":\"RH / Benefícios\",\"conteudo\":\"MANUAL DE BENEFÍCIOS...\"}"
```

Teste a busca: `GET /api/agent/rag/search?q=plano%20saúde%2090%20dias`

## 4. Testar no chat

**Chat** → selecione **Benefícios RH**.

Perguntas sugeridas:

1. "Depois de quantos dias tenho direito ao plano de saúde?"
2. "Quando posso trocar de plano?"
3. "Qual a data de hoje no servidor?" (deve usar tool de data/hora)

## 5. Testar tools (dev)

**Ferramentas (dev)** → assistente `BENEFICIOS_RH` → `search_knowledge_base` com:

```json
{ "query": "vale refeição quando credita" }
```

## Quando criar tool Java nova?

Só se precisar de algo que RAG não resolve, por exemplo:

- Consultar saldo de VR em API externa
- Abrir chamado no ServiceNow

Aí você cria uma classe `AgentTool`, reinicia a API e marca a tool no assistente.

## Checklist

- [ ] Assistente `BENEFICIOS_RH` ativo
- [ ] Tools corretas (sem `registrar_horas_extras` / HE)
- [ ] Documento indexado e busca retorna trechos
- [ ] Chat responde citando política
- [ ] `GET /api/agent/tools?assistant=BENEFICIOS_RH` lista só as tools esperadas
