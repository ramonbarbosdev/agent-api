# Agent API

Plataforma de agentes com **multi-usuário (JWT)**, conexão **Cursor por usuário** e chat em `/api/agent/chat` e `/ws/agent/chat`. Runtime via sidecar Node (`cursor-sidecar/`) e `@cursor/sdk`.

Ver **[docs/gateway-architecture.md](docs/gateway-architecture.md)**.

### Fluxo rápido (Cursor)

```bash
# 1. Registrar
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"voce@exemplo.com","password":"senha-segura-8"}'

# 2. Conectar Cursor (use o accessToken do passo 1)
curl -s -X POST http://localhost:8080/api/connections/cursor \
  -H "Authorization: Bearer SEU_JWT" \
  -H "Content-Type: application/json" \
  -d '{"apiKey":"sua-cursor-api-key","label":"Minha Cursor"}'

# 3. Criar agente (workspace = AGENT_GATEWAY_DEFAULT_PROJECT_PATH no servidor)
curl -s -X POST http://localhost:8080/api/agent/assistants \
  -H "Authorization: Bearer SEU_JWT" \
  -H "Content-Type: application/json" \
  -d '{"code":"MEU_AGENTE","name":"Meu agente","systemPrompt":"...","connectionId":"UUID_DA_CONEXAO","prependBasePrompt":false}'

# 4. Chat
curl -s -X POST http://localhost:8080/api/agent/chat \
  -H "Authorization: Bearer SEU_JWT" \
  -H "Content-Type: application/json" \
  -d '{"assistant":"MEU_AGENTE","message":"Olá"}'
```

## Stack

- Java 21
- Spring Boot 3.4
- Maven
- Spring Web + Bean Validation + Security (JWT)
- PostgreSQL + Flyway
- Cursor sidecar (`@cursor/sdk`)

## Estrutura

```text
src/main/java/com/agentapi/
├── agent/           # Controller, Service, CursorAgentRuntime
├── auth/            # JWT, registro/login
├── connection/      # Conexões Cursor por usuário
├── gateway/         # Sidecar, sessões, projetos allowlist
├── assistant/       # CRUD de agentes + prompts
├── conversation/    # ConversationService + JPA (schema agent)
├── rag/             # Documentos e busca (API)
├── tool/            # Tools manuais (/api/agent/tools)
├── config/          # Propriedades
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
agent.auth.enabled=${AGENT_AUTH_ENABLED:true}
agent.gateway.sidecar-url=${CURSOR_SIDECAR_URL:http://127.0.0.1:8791}
```

Ver `.env.exemple` para JWT, secrets, allowlist e catálogo de projetos.

CORS e WebSocket: origens do front em `config/AgentCorsOrigins.java` (edite a lista no código).

Variáveis podem ser definidas no `.env` na raiz do projeto (carregado antes do Spring Boot):

```bash
cp .env.exemple .env
```

## Pré-requisitos

1. **PostgreSQL** — instale localmente ou use `docker compose up -d` na raiz. No `.env`: `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` (veja `.env.exemple`). Os testes Maven usam H2 em memória (`profile test`).
2. **cursor-sidecar** — `cd cursor-sidecar && npm install && npm start` (token igual ao `CURSOR_SIDECAR_TOKEN` no `.env`).
3. Usuário com **API key Cursor** cadastrada em `/api/connections/cursor` e pelo menos um agente criado.

## Executar

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

Health check (leve, load balancer):

```bash
curl -s http://localhost:8080/api/agent/health
```

Diagnóstico (API + sidecar + conexão Cursor + assistente):

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

## Assistentes configuráveis (fase 9)

Assistentes ficam no PostgreSQL (`agent.assistente`, `agent.assistente_tool`). Na primeira subida sem registros, a API cria **HORAS_EXTRAS** com os prompts em `prompts/*.txt` e as tools padrão.

| Método | Endpoint |
|--------|----------|
| GET | `/api/agent/assistants` (`?includeInactive=true`) |
| GET | `/api/agent/assistants/{code}` |
| POST | `/api/agent/assistants` |
| PUT | `/api/agent/assistants/{code}` |
| DELETE | `/api/agent/assistants/{code}` |
| GET | `/api/agent/assistants/catalog/tools` |

O chat continua usando `assistant` = **código** (ex.: `HORAS_EXTRAS`). Tools são vinculadas por nome no cadastro; implementação das tools permanece em Java.

No front: menu **Assistentes** (`/agent/assistants`).

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

Listar documentos indexados:

```bash
curl -s "http://localhost:8080/api/agent/rag/documents"
```

Obter, atualizar (reindexa trechos) ou excluir:

```bash
curl -s "http://localhost:8080/api/agent/rag/documents/{documentoId}"
curl -s -X PUT "http://localhost:8080/api/agent/rag/documents/{documentoId}" \
  -H "Content-Type: application/json" \
  -d '{"titulo":"...","fonte":"...","conteudo":"..."}'
curl -s -X DELETE "http://localhost:8080/api/agent/rag/documents/{documentoId}"
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

Tools de escrita (`WRITE`) ficam bloqueadas até confirmação do usuário. O **chat** via Cursor usa o prompt do agente e persiste histórico no PostgreSQL; invocação automática de tools no loop do modelo não está no runtime Cursor (use `/api/agent/tools/invoke` para testes).

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
Frontend → AgentController → AgentService → CursorAgentRuntime → Sidecar → @cursor/sdk
```

## Roadmap

Histórico de evolução da plataforma: **[docs/ROADMAP-AGENT.md](docs/ROADMAP-AGENT.md)** (referência; runtime atual é Cursor).

## Segurança arquitetural

O LLM não acessa banco nem APIs de negócio diretamente; toda ação futura passará por tools controladas pelo backend Java.
