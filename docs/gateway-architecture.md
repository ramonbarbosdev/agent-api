# Gateway multi-usuário + Cursor

## Fluxo

1. `POST /api/auth/register` ou `/login` → JWT
2. `POST /api/connections/cursor` com API key (cifrada no Postgres)
3. `POST /api/agent/assistants` com `connectionId`, `code`, `systemPrompt` (workspace no servidor)
4. Chat: `POST /api/agent/chat` e `WS /ws/agent/chat?token=...` → `CursorAgentRuntime` → sidecar → `@cursor/sdk`

## Sidecar

```bash
cd cursor-sidecar
npm install
CURSOR_SIDECAR_TOKEN=secret npm start
```

Bind em `127.0.0.1:8791` — não expor na internet.

## Configuração (env)

| Variável | Uso |
|----------|-----|
| `AGENT_GATEWAY_DEFAULT_PROJECT_PATH` | Pasta no VPS onde o Cursor atua (cwd) |
| `AGENT_AUTH_JWT_SECRET` | JWT (mín. 32 caracteres) |
| `AGENT_API_SECRETS_KEY` | Base64 32 bytes — cifra API keys |
| `CURSOR_SIDECAR_URL` / `CURSOR_SIDECAR_TOKEN` | Comunicação Java ↔ sidecar |
| `AGENT_API_PROJECT_ROOT_ALLOWLIST` | Opcional — valida se o path default está dentro das raízes |

Catálogo `AGENT_GATEWAY_PROJECTS_*` permanece opcional para evolução futura; com `AGENT_GATEWAY_DEFAULT_PROJECT_PATH` definido, o chat não exige `projectId` no agente.

## Agentes

Cada usuário cria os próprios via API ou front. Nada é criado no startup.
