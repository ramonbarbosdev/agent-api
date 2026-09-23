package com.agentapi.tool;

/**
 * {@code READ} — consulta sem alterar dados. {@code WRITE} — exige politica do {@link com.agentapi.agent.AgentPolicy}.
 */
public enum ToolKind {
    READ,
    WRITE
}
