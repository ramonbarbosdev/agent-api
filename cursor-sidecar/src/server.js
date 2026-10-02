import express from "express";
import { Agent } from "@cursor/sdk";

const PORT = Number(process.env.CURSOR_SIDECAR_PORT || 8791);
const HOST = process.env.CURSOR_SIDECAR_HOST || "127.0.0.1";
const TOKEN = process.env.CURSOR_SIDECAR_TOKEN || "";

const sessions = new Map();

const app = express();
app.use(express.json({ limit: "2mb" }));

function auth(req, res, next) {
  if (!TOKEN) {
    return next();
  }
  const header = req.get("X-Sidecar-Token");
  if (header !== TOKEN) {
    return res.status(401).json({ error: "unauthorized" });
  }
  next();
}

app.use(auth);

app.get("/health", (_req, res) => {
  res.json({ status: "UP" });
});

app.post("/v1/validate", async (req, res) => {
  const apiKey = req.body?.apiKey;
  if (!apiKey) {
    return res.json({ ok: false });
  }
  try {
    const agent = await Agent.create({
      apiKey,
      local: { cwd: process.cwd() },
    });
    if (typeof agent.close === "function") {
      await agent.close();
    } else if (agent[Symbol.asyncDispose]) {
      await agent[Symbol.asyncDispose]();
    }
    res.json({ ok: true });
  } catch {
    res.json({ ok: false });
  }
});

app.post("/v1/sessions", async (req, res) => {
  const { sessionId, apiKey, projectPath, systemPrompt } = req.body || {};
  if (!sessionId || !apiKey || !projectPath) {
    return res.status(400).json({ error: "invalid_request" });
  }
  try {
    const agent = await Agent.create({
      apiKey,
      local: { cwd: projectPath },
    });
    const cursorAgentId = agent.id ?? sessionId;
    sessions.set(sessionId, { agent, systemPrompt: systemPrompt || "" });
    res.json({ cursorAgentId });
  } catch (err) {
    console.error("create session failed", err?.message);
    res.status(502).json({ error: "agent_start_failed" });
  }
});

app.post("/v1/sessions/:id/messages", async (req, res) => {
  const session = sessions.get(req.params.id);
  if (!session) {
    return res.status(404).json({ error: "session_not_found" });
  }
  const text = req.body?.text;
  if (!text) {
    return res.status(400).json({ error: "invalid_request" });
  }
  try {
    const prompt = session.systemPrompt
      ? `${session.systemPrompt}\n\n---\n\n${text}`
      : text;
    const run = await session.agent.send(prompt);
    session.lastRun = run;
    res.status(202).json({ runId: run.id });
  } catch (err) {
    console.error("send failed", err?.message);
    res.status(502).json({ error: "agent_error" });
  }
});

app.get("/v1/sessions/:id/stream", async (req, res) => {
  const session = sessions.get(req.params.id);
  if (!session?.lastRun) {
    res.setHeader("Content-Type", "text/event-stream");
    res.write(`data: ${JSON.stringify({ type: "session.failed", content: "no run" })}\n\n`);
    return res.end();
  }
  res.setHeader("Content-Type", "text/event-stream");
  res.setHeader("Cache-Control", "no-cache");
  res.flushHeaders?.();
  try {
    for await (const event of session.lastRun.stream()) {
      if (event.type === "assistant") {
        for (const block of event.message?.content ?? []) {
          if (block.type === "text" && block.text) {
            res.write(
              `data: ${JSON.stringify({ type: "agent.message", content: block.text })}\n\n`,
            );
          }
        }
      }
    }
    await session.lastRun.wait();
    res.write(`data: ${JSON.stringify({ type: "session.completed" })}\n\n`);
  } catch (err) {
    res.write(
      `data: ${JSON.stringify({ type: "agent.error", content: err?.message || "error" })}\n\n`,
    );
  }
  res.end();
});

app.listen(PORT, HOST, () => {
  console.log(`cursor-sidecar listening on http://${HOST}:${PORT}`);
});
