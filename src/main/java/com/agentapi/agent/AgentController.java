package com.agentapi.agent;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import com.agentapi.assistant.AssistantType;
import com.agentapi.exception.AssistantNotFoundException;
import com.agentapi.tool.ToolService;
import com.agentapi.web.AgentChatRequest;
import com.agentapi.web.AgentChatResponse;
import com.agentapi.web.AgentPlatformStatusResponse;
import com.agentapi.web.HealthResponse;
import com.agentapi.web.ToolDescriptorDto;
import com.agentapi.web.ToolInvokeRequest;
import com.agentapi.web.ToolInvokeResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final AgentService agentService;
    private final AgentStatusService agentStatusService;
    private final ToolService toolService;

    public AgentController(
            AgentService agentService,
            AgentStatusService agentStatusService,
            ToolService toolService) {
        this.agentService = agentService;
        this.agentStatusService = agentStatusService;
        this.toolService = toolService;
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

    @GetMapping(value = "/tools", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ToolDescriptorDto> listTools(@RequestParam(required = false) String assistant) {
        return toolService.listForAssistant(assistant);
    }

    @PostMapping(value = "/tools/invoke", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ToolInvokeResponse invokeTool(@Valid @RequestBody ToolInvokeRequest request) {
        AssistantType assistantType = AssistantType.fromString(request.getAssistant())
                .orElseThrow(() -> new AssistantNotFoundException(request.getAssistant()));
        return toolService.invoke(assistantType, request.getTool(), request.getArguments());
    }
}
