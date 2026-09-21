package com.agentapi.agent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.agentapi.exception.ErrorCode;
import com.agentapi.exception.GlobalExceptionHandler;
import com.agentapi.llm.LlmClient;
import com.agentapi.web.AgentPlatformStatusResponse;
import com.agentapi.web.AssistantStatusDto;
import com.agentapi.web.LlmStatusDto;
import com.agentapi.web.StatusCheck;

@WebMvcTest(AgentController.class)
@Import(GlobalExceptionHandler.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgentService agentService;

    @MockitoBean
    private LlmClient llmClient;

    @MockitoBean
    private AgentStatusService agentStatusService;

    @Test
    void healthReturnsUp() throws Exception {
        mockMvc.perform(get("/api/agent/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void statusReturnsDiagnostics() throws Exception {
        when(agentStatusService.getStatus("HORAS_EXTRAS")).thenReturn(new AgentPlatformStatusResponse(
                "UP",
                true,
                new LlmStatusDto("ollama", "http://localhost:11434", "qwen3:8b", true, true),
                "HORAS_EXTRAS",
                List.of(new AssistantStatusDto("HORAS_EXTRAS", "Assistente de Horas Extras", "Auxilia", "qwen3:8b", true)),
                List.of(new StatusCheck("api", "OK", "Agent API em execução.", null))));

        mockMvc.perform(get("/api/agent/status").param("assistant", "HORAS_EXTRAS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ready").value(true))
                .andExpect(jsonPath("$.llm.model").value("qwen3:8b"))
                .andExpect(jsonPath("$.activeAssistantId").value("HORAS_EXTRAS"));
    }

    @Test
    void chatReturnsAssistantMessage() throws Exception {
        when(agentService.chat(any())).thenReturn(new com.agentapi.web.AgentChatResponse("Olá! Como posso ajudar?"));

        mockMvc.perform(post("/api/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"assistant":"HORAS_EXTRAS","message":"Olá"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Olá! Como posso ajudar?"));
    }

    @Test
    void chatRejectsEmptyMessage() throws Exception {
        mockMvc.perform(post("/api/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"assistant":"HORAS_EXTRAS","message":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST.name()));
    }
}
