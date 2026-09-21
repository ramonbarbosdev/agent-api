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
├── conversation/    # Modelo de domínio (sem persistência)
├── tool/            # Esqueleto para tool calling futuro
├── config/          # Propriedades e beans Ollama
├── web/             # DTOs da API
└── exception/       # Erros padronizados
```

## Dependências Maven

- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-starter-test` (testes)

## Configuração

`src/main/resources/application.yml`:

```yaml
server:
  port: 8081

llm:
  provider: ollama

ollama:
  base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
  model: ${OLLAMA_MODEL:qwen3:8b}
  timeout: ${OLLAMA_TIMEOUT:60s}
```

## Pré-requisitos

1. [Ollama](https://ollama.com/) em execução (porta `11434`).
2. Modelo baixado, por exemplo:

```bash
ollama pull qwen3:8b
```

## Executar

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8081`.

## Testar (cURL / Postman)

```bash
curl -s -X POST http://localhost:8081/api/agent/chat \
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
curl -s -X POST http://localhost:8081/api/agent/chat \
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

## Próximos passos

1. **Tool calling** — loop no `AgentEngine`, `ToolExecutor` e integração com APIs de negócio (horas extras).
2. **AgentContext real** — `tenantId`, `userId`, permissões a partir de autenticação.
3. **Conversas** — `ConversationService` + PostgreSQL para histórico.
4. **Novos assistentes** — enum, prompts em `resources/prompts/`, ferramentas por assistente.
5. **Confirmação de escrita** — implementar `AgentPolicy` para operações sensíveis.
6. **Observabilidade** — correlation id, métricas e health do Ollama.

## Segurança arquitetural

O LLM não acessa banco nem APIs de negócio diretamente; toda ação futura passará por tools controladas pelo backend Java.
