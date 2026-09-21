package com.agentapi.agent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

@WebMvcTest(AgentController.class)
@Import(GlobalExceptionHandler.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgentService agentService;

    @MockitoBean
    private LlmClient llmClient;

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
