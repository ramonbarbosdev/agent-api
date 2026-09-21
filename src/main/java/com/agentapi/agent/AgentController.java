package com.agentapi.agent;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.agentapi.web.AgentChatRequest;
import com.agentapi.web.AgentChatResponse;
import com.agentapi.web.AgentPlatformStatusResponse;
import com.agentapi.web.HealthResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;
    private final AgentStatusService agentStatusService;

    public AgentController(AgentService agentService, AgentStatusService agentStatusService) {
        this.agentService = agentService;
        this.agentStatusService = agentStatusService;
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public HealthResponse health() {
        return new HealthResponse("UP");
    }

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public AgentPlatformStatusResponse status(@RequestParam(required = false) String assistant) {
        return agentStatusService.getStatus(assistant);
    }

    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AgentChatResponse chat(@Valid @RequestBody AgentChatRequest request) {
        return agentService.chat(request);
    }
}
