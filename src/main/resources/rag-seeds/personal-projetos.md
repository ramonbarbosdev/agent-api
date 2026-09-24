# Projetos e prioridades

Resumo: mapa rápido dos projetos do Ramon para o assistente consultar.

## agent-api
- API Spring Boot do assistente: chat, WebSocket, RAG, tools
- Assistente foco: PERSONAL (assistente pessoal)
- LLM: Ollama (local ou VPS)

## agent-front
- Playground de chat, base de conhecimento, assistentes
- Aponta para a API via VITE_AGENT_API_URL

## Objetivos atuais
- Evoluir o assistente pessoal (conversa, RAG, ações futuras)
- Manter agent-api e agent-front alinhados
- Deploy estável na VPS com Tailscale

## Próximas integrações (planejado, não implementado no assistente pessoal ainda)
- APIs na VPS (ex.: horas-extras) via tools com confirmação
- Mais documentos pessoais nesta base RAG

## Prioridade da semana (exemplo — atualize)
1. Consolidar RAG pessoal e testar perguntas no playground
2. Deploy estável na VPS com Tailscale
