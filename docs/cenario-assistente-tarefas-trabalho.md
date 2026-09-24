# Cenário: assistente Tarefas do trabalho (`TAREFAS_TRABALHO`)

Novo assistente **sem código Java**: só configuração na UI + base de conhecimento (RAG).

Tools sugeridas: `search_knowledge_base`, `obter_data_hora_servidor`.

O assistente **organiza e prioriza** tarefas na conversa (listas, prazos, próximos passos). Ele **não grava** tarefas em banco nem integra com Jira/Teams — isso seria fase futura com tools dedicadas.

## 1. Criar o assistente (UI)


| Campo                | Valor                                                                         |
| -------------------- | ----------------------------------------------------------------------------- |
| Código               | `TAREFAS_TRABALHO`                                                            |
| Nome                 | Tarefas do trabalho                                                           |
| Descrição            | Priorização, planejamento do dia/semana e alinhamento com o método da empresa |
| Prefixar prompt base | Sim                                                                           |
| Modelo               | (vazio ou o que você usa no Ollama)                                           |
| Ativo                | Sim                                                                           |
| Injetar RAG          | Sim                                                                           |
| RAG top-K            | 4                                                                             |
| Tools                | `search_knowledge_base`, `obter_data_hora_servidor`                           |




### Prompt de sistema (domínio)

```text
Você é o assistente de tarefas e produtividade no trabalho.

Ajude o colaborador a:
- listar e priorizar tarefas (urgente/importante, dependências, prazos);
- sugerir um plano para hoje ou para a semana;
- quebrar tarefas grandes em passos menores;
- identificar bloqueios e o que pedir ajuda ao gestor.

Use search_knowledge_base quando a pergunta envolver método interno (OKR, rituais, ferramentas oficiais, SLA).
Use obter_data_hora_servidor quando precisar da data/hora oficial para prazos ou “o que fazer hoje”.

Não invente políticas da empresa. Se a base não tiver a resposta, diga e sugira o canal correto (gestor, PM, TI).

Você não cria chamados nem altera sistemas externos; apenas orienta e estrutura o trabalho na conversa.
Quando o usuário pedir para “salvar” tarefas, confirme a lista em markdown e sugira que ele copie para a ferramenta oficial (Planner, Jira, etc.).
```

Salvar → **Criar assistente**.

## 2. Indexar conhecimento (opcional mas recomendado)

**Base de conhecimento** → ingerir documento com o método da sua equipe/empresa.

Exemplo de conteúdo:

```text
MÉTODO DE TRABALHO — TIME PRODUTO

Priorização: matriz urgente/importante. “Urgente e importante” no mesmo dia; “importante não urgente” na semana.
Daily: máximo 15 min; bloqueios escalados ao tech lead antes do meio-dia.

Ferramenta oficial de tarefas: Jira (projeto PROD). Não usar lista paralela em e-mail para compromissos de sprint.

Prazos: entregas para cliente seguem SLA de 5 dias úteis após aprovação do PO.

Reuniões: blocos de foco terça e quinta 9h–12h — evitar marcar reuniões nesse horário.
```



## 3. Testar no chat

**Chat** → **Tarefas do trabalho**.

Perguntas sugeridas:

1. "Tenho relatório até sexta, bug crítico e revisão de PR. Como priorizo?"
2. "Monta um plano para hoje considerando que são 10h."
3. "Qual o SLA de entrega para cliente?" (deve buscar na base)



## 4. Quando evoluir (só se você pedir)


| Necessidade                      | Caminho                                    |
| -------------------------------- | ------------------------------------------ |
| Só orientação + política interna | Este cenário (UI + RAG)                    |
| Registrar tarefa em API/Jira     | Nova `AgentTool` + vincular no assistente  |
| Lista persistente por usuário    | Modelo de dados + tools de leitura/escrita |




## Checklist

- [ ] Assistente `TAREFAS_TRABALHO` ativo
- [ ] Sem tools de horas extras
- [ ] Documento de método indexado (se usar RAG)
- [ ] Chat prioriza e estrutura sem afirmar que gravou no Jira