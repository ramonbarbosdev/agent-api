# Teste prático: injeção RAG vs tool `search_knowledge_base`

Objetivo: ver na prática **quando o texto vai no prompt automaticamente** e **quando o modelo precisa chamar a tool**.

## Pré-requisitos

- API + Postgres + Ollama no ar.
- Front em `http://localhost:5173`.
- RAG ligado: `AGENT_RAG_ENABLED=true` (padrão).

## 1. Documento “laboratório” (frase única)

**Base de conhecimento → Indexar** (ou use um assistente de teste só para isso):

| Campo | Valor |
|-------|--------|
| Título | Laboratório RAG vs tool |
| Fonte | TESTE / LAB |
| Conteúdo | (abaixo) |

```text
PROTOCOLO LAB-RAG-2026 — USO INTERNO DE TESTE

O prazo para revisão voluntária de benefícios é exatamente 17 dias úteis,
contados a partir da abertura do chamado no portal.

Código de referência que deve aparecer na resposta correta: LAB-RAG-2026.
Não confundir com prazos de férias ou horas extras.
```

Confirme na aba **Indexados** e em **Testar busca**:

- `LAB-RAG-2026` — deve sempre retornar trecho.
- A **pergunta inteira** do chat pode falhar em FTS antigo (todos os termos precisam existir no chunk); a API tenta `websearch_to_tsquery` e fallback no código `LAB-RAG-2026`.

Se a busca com a frase longa vier vazia e `LAB-RAG-2026` vier com hit, o chat **sem contexto** tende a inventar número (ex.: 21 em vez de 17).

## 2. Quatro modos (matriz)

Crie **dois assistentes de laboratório** (ou altere um e anote o resultado antes de mudar).

| Modo | Nome sugerido | Injetar RAG | Tool `search_knowledge_base` | Variável global (opcional) |
|------|----------------|-------------|------------------------------|----------------------------|
| **A – Só injeção** | `LAB_SO_RAG` | Sim | **Desmarcada** | `AGENT_RAG_INJECT=true` (padrão) |
| **B – Só tool** | `LAB_SO_TOOL` | Não | **Marcada** | `AGENT_RAG_INJECT=false` **recomendado** para isolar |
| **C – Os dois** | `LAB_RAG_E_TOOL` | Sim | Marcada | padrão |
| **D – Nenhum** | `LAB_SEM_KB` | Não | Desmarcada | — |

Prompt para **modo A – só injeção RAG** (`LAB_SO_RAG`, sem tools):

```text
Você é um assistente de teste de conhecimento interno.

A seção "Base de conhecimento (trechos já recuperados...)" no prompt JÁ é o resultado da busca.
Use esse bloco para responder; você não tem ferramentas de busca neste assistente.

Responda sobre prazos de benefícios só com base nesses trechos.
Se o bloco estiver vazio ou não tiver o dado, diga que não encontrou na base.
Cite título/fonte do trecho quando usar um fato.
```

Prompt para **modos B/C** (com `search_knowledge_base` marcada):

```text
Você é um assistente de teste de conhecimento interno.

Use search_knowledge_base antes de afirmar prazos se o bloco de base de conhecimento não tiver o dado.
Se os trechos já recuperados no prompt bastarem, responda com eles.
Cite o código LAB-RAG-2026 quando o documento de teste for recuperado.
```

**Só tool (modo B):** reinicie a API com injeção global desligada, senão o modo A ainda injeta trechos mesmo com “Injetar RAG” off no assistente:

```properties
# application.properties ou variável de ambiente
agent.rag.inject-into-system-prompt=false
# ou AGENT_RAG_INJECT=false
```

A tool continua funcionando com `agent.rag.enabled=true`.

## 3. Perguntas para rodar no Chat

Use **nova conversa** (limpar thread ou novo `conversationId`) a cada modo.

| # | Pergunta | O que observar |
|---|----------|----------------|
| 1 | `Qual o código LAB-RAG-2026 e quantos dias úteis para revisão voluntária de benefícios?` | Resposta correta: **17 dias úteis** + menção **LAB-RAG-2026**. |
| 2 | `E se eu não abrir chamado no portal?` (follow-up vago) | RAG usa só a **última mensagem** na busca automática; a tool pode montar `query` melhor. |
| 3 | `Qual o prazo de férias nesse protocolo?` | Documento diz que **não** é férias — resposta deve negar ou não inventar. |

### Resultado esperado por modo

| Modo | Pergunta 1 | Log da API |
|------|------------|------------|
| **A – Só RAG** | Costuma acertar se a mensagem contém termos da busca | **Sem** `Tool execution started (tool=search_knowledge_base` |
| **B – Só tool** | Acerta **se o modelo chamar** a tool | **Com** `Tool execution started (tool=search_knowledge_base` |
| **C – Ambos** | Maior chance de acerto (modelos pequenos) | Pode ter tool **ou não**; mesmo assim pode já vir contexto no prompt |
| **D – Nenhum** | Não deve citar LAB-RAG-2026 com fidelidade | Sem tool; sem bloco de contexto recuperado |

## 4. Como saber o que aconteceu

### Console da API (Java)

- **Tool usada:** linha  
  `Tool execution started (tool=search_knowledge_base, assistant=..., conversationId=...)`
- **Turno LLM:**  
  `LLM response received (... toolCalls=1 ...)` indica que o modelo pediu tool; `toolCalls=0` no primeiro passo com resposta final direta.

### Ferramentas (dev)

- Assistente = o do teste.
- Tool = `search_knowledge_base`.
- JSON: `{ "query": "LAB-RAG-2026 revisão benefícios" }`  
  Deve retornar trechos — prova que a **base** está ok independente do chat.

### Base de conhecimento → Testar busca

- Mesma busca que a tool usa por baixo (`RagSearchService`).
- Se aqui não retorna nada, nem RAG nem tool vão ajudar.

## 5. Atalho com curl (sem LLM)

```bash
# Busca direta (igual à tool)
curl -s "http://localhost:8080/api/agent/rag/search?q=LAB-RAG-2026"

# Chat modo C (substitua conversationId se quiser)
curl -s -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d "{\"assistant\":\"LAB_RAG_E_TOOL\",\"message\":\"Qual o protocolo LAB-RAG-2026?\"}"
```

## 6. Modelo pequeno (ex.: qwen3:0.6b)

- **Só tool:** pode **não** chamar a tool e inventar — compare com modo **C**.
- **Só RAG:** às vezes responde sem “saber” que buscou documento (contexto já está no prompt).
- Para laboratório, um modelo um pouco maior (ex. `qwen3:8b` no cadastro do assistente) deixa a diferença A vs B mais clara.

## Checklist rápido

- [ ] Documento LAB indexado e busca manual ok
- [ ] Modo A: acerta pergunta 1, log **sem** search_knowledge_base
- [ ] Modo B: `AGENT_RAG_INJECT=false`, acerta se log **com** tool
- [ ] Modo D: não cita protocolo falso
- [ ] Modo C: comportamento “mais estável” que B sozinho

## Resumo mental

| Mecanismo | Quem dispara | Query de busca |
|-----------|--------------|----------------|
| **Injetar RAG** | API antes do LLM | Texto da **mensagem do usuário** no turno |
| **search_knowledge_base** | LLM no meio do turno | Parâmetro **`query`** escolhido pelo modelo |

Ambos leem a **mesma** base global; nenhum filtra por código do assistente hoje.
