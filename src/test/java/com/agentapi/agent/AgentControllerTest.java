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
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.agentapi.exception.ErrorCode;
import com.agentapi.exception.GlobalExceptionHandler;
import com.agentapi.tool.ToolService;
import com.agentapi.web.AgentPlatformStatusResponse;
import com.agentapi.web.AssistantStatusDto;
import com.agentapi.web.CursorStatusDto;
import com.agentapi.web.LlmStatusDto;
import com.agentapi.web.StatusCheck;

@WebMvcTest(controllers = AgentController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import(GlobalExceptionHandler.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgentService agentService;

    @MockitoBean
    private AgentStatusService agentStatusService;

    @MockitoBean
    private ToolService toolService;

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
                new LlmStatusDto("cursor", "", "", true, true),
                new CursorStatusDto(true, true, true),
                "HORAS_EXTRAS",
                List.of(new AssistantStatusDto("HORAS_EXTRAS", "Assistente de Horas Extras", "Auxilia", "cursor", true)),
                List.of(new StatusCheck("api", "OK", "Agent API em execução.", null))));

        mockMvc.perform(get("/api/agent/status").param("assistant", "HORAS_EXTRAS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ready").value(true))
                .andExpect(jsonPath("$.llm.provider").value("cursor"))
                .andExpect(jsonPath("$.activeAssistantId").value("HORAS_EXTRAS"));
    }

    @Test
    void chatReturnsAssistantMessage() throws Exception {
        when(agentService.chat(any())).thenReturn(new com.agentapi.web.AgentChatResponse(
                "Olá! Como posso ajudar?",
                "550e8400-e29b-41d4-a716-446655440000"));

        mockMvc.perform(post("/api/agent/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"assistant":"HORAS_EXTRAS","message":"Olá"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Olá! Como posso ajudar?"))
                .andExpect(jsonPath("$.conversationId").value("550e8400-e29b-41d4-a716-446655440000"));
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
