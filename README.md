# Agent API

API núcleo de uma plataforma de assistentes LLM. Este MVP expõe chat HTTP com o assistente **HORAS_EXTRAS**, orquestrado pelo **Agent Engine** e integrado ao **Ollama** como provedor local de LLM.

## Stack

- Java 21
- Spring Boot 3.4
- Maven
- Spring Web + Bean Validation
- Ollama (HTTP)

Sem banco de dados, autenticação ou WhatsApp neste MVP.

## Estrutura

```text
src/main/java/com/agentapi/
├── agent/           # Controller, Service, Engine, Context, Policy
├── assistant/       # Tipos e registro de assistentes + prompts
├── llm/             # LlmClient e OllamaClient
├── conversation/    # ConversationService + JPA (schema agent)
├── model/           # AuditableEntity (padrão eSimples)
├── tool/            # Esqueleto para tool calling futuro
├── config/          # Propriedades e beans Ollama
├── web/             # DTOs da API
└── exception/       # Erros padronizados
```

Banco: PostgreSQL + Flyway, schema **`agent`** (`conversa`, `mensagem_conversa`). Deploy **single-tenant** (uma organização por instalação). Detalhes em **[docs/arquitetura-banco-atual.md](docs/arquitetura-banco-atual.md)**.

## Dependências Maven

- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-starter-data-jpa`
- PostgreSQL + Flyway (conversas)
- `spring-boot-starter-test` (testes com H2 em memória)

## Configuração

`src/main/resources/application.properties`:

```properties
server.port=${SERVER_PORT:8080}

llm.provider=ollama

ollama.base-url=${OLLAMA_BASE_URL:http://localhost:11434}
ollama.model=${OLLAMA_MODEL:qwen3:8b}
ollama.timeout=${OLLAMA_TIMEOUT:60s}
```

CORS para o front em dev (`http://localhost:5173`) está em `config/WebConfig.java` (não usa `.env`).

Variáveis podem ser definidas no `.env` na raiz do projeto (carregado antes do Spring Boot):

```bash
cp .env.exemple .env
```

## Pré-requisitos

1. **PostgreSQL** — instale localmente ou use `docker compose up -d` na raiz (banco `agent_api`, usuário/senha `agent`). No `.env`: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` (veja `.env.exemple`). Sem Postgres a API não sobe; os testes Maven usam H2 em memória (`profile test`).
2. [Ollama](https://ollama.com/) em execução (porta `11434`).
3. Modelo baixado, por exemplo:

```bash
ollama pull qwen3:8b
```

## Executar

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

Health check (leve, load balancer):

```bash
curl -s http://localhost:8080/api/agent/health
```

Diagnóstico completo (API + Ollama + modelo + assistente):

```bash
curl -s "http://localhost:8080/api/agent/status?assistant=HORAS_EXTRAS"
```

## Integração com o front (Vite)

O playground em `http://localhost:5173` chama a API com CORS habilitado para essa origem. O chat aceita histórico opcional:

```json
{
  "assistant": "HORAS_EXTRAS",
  "message": "Nova pergunta",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "history": [
    { "role": "user", "content": "Olá" },
    { "role": "assistant", "content": "Olá! Como posso ajudar?" }
  ]
}
```

`conversationId` é opcional: se omitido, a API gera um UUID e devolve em cada resposta. Mensagens são gravadas em PostgreSQL; o histórico enviado pelo cliente só é usado se a thread ainda não tiver mensagens no banco. O system prompt de cada assistente é `prompts/base-system.txt` + prompt de domínio (ex.: `horas-extras-system.txt`).

## RAG (fase 6)

Com PostgreSQL e Flyway aplicados, a API indexa documentos no schema `agent` e injeta trechos relevantes no system prompt. Seed de desenvolvimento: politica de horas extras (`V4__rag_seed_politica_horas_extras.sql`).

Busca manual:

```bash
curl -s "http://localhost:8080/api/agent/rag/search?q=aprovacao%20horas%20extras"
```

Ingerir documento:

```bash
curl -s -X POST http://localhost:8080/api/agent/rag/documents \
  -H "Content-Type: application/json" \
  -d '{"titulo":"Manual HE","fonte":"RH 2026","conteudo":"Texto completo do manual..."}'
```

Tool do assistente: `search_knowledge_base`. Desligar RAG: `AGENT_RAG_ENABLED=false`.

## Ferramentas (tools) — fase 3

Listar tools do assistente:

```bash
curl -s "http://localhost:8080/api/agent/tools?assistant=HORAS_EXTRAS"
```

Invocar manualmente (teste / integração):

```bash
curl -s -X POST http://localhost:8080/api/agent/tools/invoke \
  -H "Content-Type: application/json" \
  -d '{"assistant":"HORAS_EXTRAS","tool":"consultar_politica_horas_extras","arguments":"{\"topico\":\"aprovacao\"}"}'
```

Tools de escrita (`WRITE`) ficam bloqueadas até confirmação do usuário. No **chat**, o `AgentTurnContextFactory` monta system (prompt + memória/RAG quando existir + tools), histórico do PostgreSQL e truncagem (`AGENT_MAX_CONTEXT_CHARS`); o `AgentEngine` executa o loop tool calling (`AGENT_MAX_TOOL_STEPS`). Modelos pequenos podem não invocar tools — prefira modelos com suporte a function calling quando for crítico.

## Testar (cURL / Postman)

```bash
curl -s -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"assistant":"HORAS_EXTRAS","message":"Olá, quem é você?"}'
```

Resposta esperada (exemplo):

```json
{
  "message": "..."
}
```

Assistente inválido:

```bash
curl -s -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"assistant":"INEXISTENTE","message":"Olá"}'
```

## Testes automatizados

```bash
mvn test
```

## Fluxo

```text
Frontend → AgentController → AgentService → AgentEngine → OllamaClient → Ollama → LLM
```

## Treinar / melhorar o assistente

Passo a passo (prompt, Modelfile, fine-tuning e RAG): **[docs/TUTORIAL-TREINAMENTO.md](docs/TUTORIAL-TREINAMENTO.md)**.

## Roadmap da plataforma Agent

Evolução em 8 fases (contrato → memória → tools → tool calling → contexto no engine → RAG → avaliação → fine-tuning): **[docs/ROADMAP-AGENT.md](docs/ROADMAP-AGENT.md)**.

## Próximos passos (resumo)

Ver checklist detalhado no roadmap. Em linha geral: compor prompts (`base-system` + domínio), persistir conversas, implementar tools e o loop de tool calling no `AgentEngine`, depois RAG e avaliação antes de fine-tuning.

## Segurança arquitetural

O LLM não acessa banco nem APIs de negócio diretamente; toda ação futura passará por tools controladas pelo backend Java.
