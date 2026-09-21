package com.agentapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.agentapi.llm.LlmClient;

@SpringBootTest
class AgentApiApplicationTests {

    @MockitoBean
    private LlmClient llmClient;

    @Test
    void contextLoads() {
    }
}
