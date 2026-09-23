# Arquitetura atual do banco de dados (Agent API)

Segue **convenções de nomenclatura e Flyway** do eSimples, com deploy **single-tenant**: **uma organização por instalação** (sem coluna `id_organizacao` nas tabelas do Agent).

Referência de estilo: `C:\Users\Ramon\Documents\dev\Java\esimples-api\docs\arquitetura-banco-atual.md`

## 1. Visão geral

- **SGBD:** PostgreSQL.
- **Modelo de tenancy:** **single-tenant** — o banco inteiro pertence a uma única organização/cliente. Não há particionamento por `id_organizacao` no módulo Agent.
- **Config:** `agent.deployment.tenant-mode=single` (padrão). Valor `multi` reservado para evolução futura SaaS.
- **Evolução:** Flyway em `src/main/resources/db/migration`.
- **Runtime:** `spring.jpa.hibernate.ddl-auto=validate`.
- **Auditoria:** `AuditableEntity` (`dt_criacao`, `dt_atualizacao`).

## 2. Schemas

### `public`

Sem tabelas do Agent neste MVP. Integração futura com cadastros do eSimples (se compartilhar o mesmo cluster Postgres) pode usar `public` sem exigir multi-tenant no Agent.

### `agent`

Conversas e mensagens dos assistentes LLM.

| Tabela | Responsabilidade |
|--------|------------------|
| `agent.conversa` | Thread de chat |
| `agent.mensagem_conversa` | Mensagens `user` / `assistant` |

## 3. Principais tabelas

### `agent.conversa`

- **PK:** `id_conversa` (UUID).
- **Usuário:** `id_usuario` (UUID, opcional até autenticação).
- **Assistente:** `nm_assistente` (ex.: `HORAS_EXTRAS`).
- **Auditoria:** `dt_criacao`, `dt_atualizacao`.

Não há `id_organizacao`: a organização é implícita na instalação (single-tenant).

### `agent.mensagem_conversa`

- **PK:** `id_mensagemconversa` (UUID).
- **FK:** `id_conversa` → `agent.conversa` (`ON DELETE CASCADE`).
- **Papel:** `tp_papel` (`user` | `assistant`).
- **Conteúdo:** `ds_conteudo` (TEXT).
- **Auditoria:** `dt_criacao`, `dt_atualizacao`.
- **Índice:** `ix_mensagem_conversa_conversa_criacao`.

## 4. Nomenclatura (padrão eSimples)

| Prefixo | Uso |
|---------|-----|
| `id_` | Identificadores |
| `nm_` | Nome / código (assistente) |
| `ds_` | Texto |
| `tp_` | Tipo enumerado |
| `dt_` | Data/hora |

## 5. Single-tenant vs eSimples SaaS

| eSimples (SaaS) | Agent API (este projeto) |
|-----------------|---------------------------|
| `id_organizacao` em quase todas as tabelas | **Sem** `id_organizacao` |
| Vários clientes no mesmo banco | **Um** cliente por banco/deploy |
| `TenantEntity` / `TenantContextService` | `AgentContext` com `userId` (opcional), sem tenant |

Se no futuro for necessário multi-tenant, será nova migration + modo `agent.deployment.tenant-mode=multi` — fora do escopo atual.

## 6. Código Java

| Camada | Classe |
|--------|--------|
| Deploy | `AgentDeploymentProperties` |
| Auditoria | `AuditableEntity` |
| Persistência | `ConversaEntity`, `MensagemConversaEntity` |
| Serviço | `ConversationService` |

## 7. Migrations

| Versão | Arquivo |
|--------|---------|
| V1 | `V1__create_schema_agent.sql` — schema `agent`, tabelas single-tenant |
| V2 | `V2__single_tenant_remove_organizacao.sql` — remove `id_organizacao` se existir (dev que aplicou V1 antigo) |

## 8. Testes

Profile `test`: H2 + schema `agent`, Flyway desligado.

## 8.1 RAG (fase 6)

| Tabela | Descrição |
|--------|-----------|
| `agent.documento` | Metadados (`nm_titulo`, `nm_fonte`) |
| `agent.documento_chunk` | Trechos (`ds_conteudo`) + `ds_busca` tsvector (PostgreSQL) |

## 9. Próximos passos (banco)

1. Autenticação preenchendo `id_usuario` em `AgentContext`.
2. Tools / RAG no schema `agent`.
3. Multi-tenant só se o produto mudar de single para SaaS (migrations dedicadas).
