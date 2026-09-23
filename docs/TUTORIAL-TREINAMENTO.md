# Tutorial: como “treinar” o assistente com Ollama

Este guia explica, do mais simples ao mais avançado, como fazer o assistente **HORAS_EXTRAS** (e futuros assistentes) se comportar melhor. A **Agent API** não treina redes neurais; ela envia **prompt de sistema** + **histórico** para o Ollama.

---

## Antes de começar: o que é “treinar” aqui?

| Termo | O que é | Onde fazer |
|--------|---------|------------|
| **Usar um modelo** | Baixar pesos prontos (Qwen, Llama, etc.) | `ollama pull` no servidor Ollama |
| **Instruir o assistente** | Regras, tom, exemplos no texto do system prompt | `src/main/resources/prompts/*.txt` |
| **Customizar no Ollama** | Mesmo modelo + system/temperatura fixos em um nome novo | `Modelfile` + `ollama create` |
| **Fine-tuning** | Alterar pesos com dataset (GPU, ferramentas externas) | Unsloth, LLaMA-Factory, etc. → GGUF → Ollama |
| **RAG** | Consultar documentos e colar trechos no contexto | Futuro na plataforma (não é treino de modelo) |

Para o MVP da **agent-api**, o caminho recomendado é: **modelo adequado** + **prompt bem escrito** (+ RAG depois, se precisar de PDFs/normas).

---

## Nível 1 — Instalar modelo no Ollama (5 minutos)

O treino pesado já foi feito pelos criadores do modelo. Você só **baixa** e **aponta** a API.

### 1.1 Onde o Ollama roda

- **Mesma máquina da API:** `OLLAMA_BASE_URL=http://localhost:11434`
- **Outro PC na rede (ex.: servidor):** `OLLAMA_BASE_URL=http://192.168.0.28:11434`  
  No servidor: Ollama ativo, `OLLAMA_HOST=0.0.0.0:11434`, firewall liberando porta `11434`.

### 1.2 Baixar o modelo

No computador **onde o Ollama está instalado**:

```bash
# modelo menor / mais rápido
ollama pull qwen3:0.6b

# modelo maior / respostas melhores (mais RAM e mais lento)
ollama pull qwen3:8b
```

Listar modelos instalados:

```bash
ollama list
```

### 1.3 Configurar a Agent API

Arquivo `.env` na raiz do projeto:

```env
OLLAMA_BASE_URL=http://192.168.0.28:11434
OLLAMA_MODEL=qwen3:0.6b
OLLAMA_TIMEOUT=180s
```

Reinicie a API (`mvn spring-boot:run` ou pelo IDE).

### 1.4 Validar

```bash
curl -s "http://localhost:8080/api/agent/status?assistant=HORAS_EXTRAS"
```

Confira: `ready: true`, modelo listado em `llm`, checks do Ollama OK.

Teste de chat:

```bash
curl -s -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d "{\"assistant\":\"HORAS_EXTRAS\",\"message\":\"Quem é você e o que pode fazer?\"}"
```

---

## Nível 2 — “Treinar” com prompt de sistema (recomendado)

É o método principal deste repositório. Cada mensagem de chat inclui o conteúdo de:

`src/main/resources/prompts/horas-extras-system.txt`

O `AssistantService` carrega esse arquivo na subida da API e o `AgentEngine` envia como mensagem `system` ao Ollama.

### 2.1 Estrutura de um bom system prompt

Use seções claras (pode copiar e adaptar):

```text
# Papel
Você é o assistente de horas extras da [NOME DA EMPRESA].

# Objetivo
Ajudar gestores e colaboradores a entender regras e fluxos de horas extras.

# Regras obrigatórias
- Responda em português do Brasil.
- Não invente valores, saldos ou aprovações.
- Se não tiver dado no contexto, diga que não sabe e oriente o próximo passo.
- Nunca diga que registrou ou alterou algo no sistema (ainda não há ferramentas conectadas).

# Conhecimento fixo (resumo da política interna)
- Jornada padrão: ...
- Limite de HE por mês: ...
- Quem aprova: ...

# Formato das respostas
- Respostas curtas em tópicos quando houver passos.
- Uma pergunta de esclarecimento por vez, se faltar informação.

# Exemplos (few-shot)
Usuário: Posso lançar 20h extras sem aprovação?
Assistente: Não. Segundo a política interna, ... [explique e cite o fluxo de aprovação].
```

**Dica:** 2–5 exemplos “Usuário / Assistente” costumam melhorar mais que parágrafos genéricos.

### 2.2 Editar e testar

1. Salve `horas-extras-system.txt`.
2. Reinicie a API (o prompt é lido no startup).
3. Teste no playground ou com `curl` (várias perguntas repetidas para comparar).
4. Ajuste frases que o modelo ainda “inventa” — reforce com “NUNCA …” e exemplos negativos.

### 2.3 Novo assistente (mesmo padrão)

1. Criar `src/main/resources/prompts/meu-assistente-system.txt`
2. Adicionar valor em `AssistantType` e registro em `AssistantService`
3. Usar `"assistant": "MEU_TIPO"` no POST `/api/agent/chat`

---

## Nível 3 — Modelfile no Ollama (variante nomeada)

Útil quando você quer o **mesmo** comportamento base em qualquer cliente (não só na Java API), ou parâmetros fixos (temperatura, contexto).

### 3.1 Criar o arquivo `Modelfile`

No servidor Ollama, pasta à sua escolha:

```dockerfile
FROM qwen3:0.6b

PARAMETER temperature 0.2
PARAMETER num_ctx 8192

SYSTEM """
Você é o assistente de horas extras.
[mesmo texto que você colocaria no horas-extras-system.txt]
"""
```

### 3.2 Criar o modelo customizado

```bash
ollama create horas-extras-qwen -f Modelfile
```

Testar direto no Ollama:

```bash
ollama run horas-extras-qwen "Explique o fluxo de aprovação de HE"
```

### 3.3 Usar na API

`.env`:

```env
OLLAMA_MODEL=horas-extras-qwen
```

**Evite duplicar:** se o system prompt está no Modelfile **e** no `.txt` da API, o modelo recebe **dois** system prompts. Escolha um lugar como “fonte da verdade” ou deixe o Modelfile só com `PARAMETER` e mantenha o texto na API.

---

## Nível 4 — Fine-tuning de verdade (avançado)

Só faça isso se prompt + exemplos + RAG não bastarem e você tiver **dataset grande**, **GPU** e tempo para validar qualidade.

### 4.1 Formato típico de dataset (instrução)

Arquivo `dataset.jsonl` (uma linha = um exemplo):

```json
{"instruction": "O colaborador pode compensar HE com folga?", "input": "Política: compensação só com acordo do gestor.", "output": "Sim, mediante acordo do gestor, conforme a política interna. Não há compensação automática."}
{"instruction": "Quantas horas extras sem aprovação?", "input": "", "output": "Nenhuma hora extra deve ser registrada sem aprovação prévia do gestor imediato."}
```

Quanto mais exemplos **reais** (anonimizados) da sua empresa, melhor — centenas a milhares para efeito visível.

### 4.2 Fluxo resumido (fora do Ollama)

1. Escolher base (ex.: `Qwen2.5-0.5B-Instruct` ou similar compatível com sua GPU).
2. Treinar com **Unsloth**, **Axolotl** ou **LLaMA-Factory** (LoRA/QLoRA).
3. Exportar para **GGUF** (ex.: via `llama.cpp` / scripts da ferramenta).
4. Importar no Ollama com Modelfile:

```dockerfile
FROM ./meu-modelo-finetuned.gguf

PARAMETER temperature 0.2
```

```bash
ollama create horas-extras-finetuned -f Modelfile
```

5. Apontar `OLLAMA_MODEL=horas-extras-finetuned` e medir no mesmo conjunto de perguntas de teste.

### 4.3 Riscos

- Modelo pequeno pode **memorizar** frases erradas do dataset.
- Fine-tune exige **re-treino** quando a política da empresa mudar.
- Para normas e PDFs longos, **RAG** costuma ser mais barato e atualizável.

---

## Nível 5 — RAG (conhecimento em documentos, sem fine-tune)

Quando a resposta depende de PDFs, CLT interna, manuais:

1. Dividir documentos em trechos (chunks).
2. Indexar com embeddings (vetor + busca).
3. Na pergunta do usuário: buscar trechos relevantes.
4. Injetar no prompt: “Use apenas o contexto abaixo: …”

Isso será evolução da plataforma (tools + contexto no `AgentEngine`). Até lá, você pode colar **resumos curtos** da política no `horas-extras-system.txt` (Nível 2).

---

## Checklist rápido

- [ ] Ollama acessível da máquina da API (`curl http://IP:11434/api/tags`)
- [ ] `ollama pull` do modelo desejado
- [ ] `.env` com `OLLAMA_BASE_URL`, `OLLAMA_MODEL`, timeout alto na primeira inferência
- [ ] Prompt revisado em `prompts/horas-extras-system.txt`
- [ ] `/api/agent/status` com `ready: true`
- [ ] Testes manuais com perguntas difíceis (inventar dado, pedir ação no sistema)

---

## Referências

- [Ollama – Modelfile](https://github.com/ollama/ollama/blob/main/docs/modelfile.md)
- [Ollama – API HTTP](https://github.com/ollama/ollama/blob/main/docs/api.md)
- Documentação do projeto: `README.md` (execução e endpoints)

---

## Fluxo na Agent API (recordatório)

```text
POST /api/agent/chat
  → AgentEngine monta mensagens: [system = prompt do arquivo], [history], [user]
  → OllamaClient → POST /api/chat no Ollama
  → resposta JSON para o front
```

O “treinamento” do produto, hoje, é **iterar o prompt** e **escolher o modelo**; fine-tuning é opcional e posterior.
