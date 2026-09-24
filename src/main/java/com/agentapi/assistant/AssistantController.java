package com.agentapi.assistant;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.agentapi.tool.AgentTool;
import com.agentapi.tool.ToolRegistry;
import com.agentapi.web.AssistantRequest;
import com.agentapi.web.AssistantResponse;
import com.agentapi.web.ToolCatalogEntryDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/agent/assistants")
public class AssistantController {

    private final AssistantConfigService assistantConfigService;
    private final ToolRegistry toolRegistry;

    public AssistantController(AssistantConfigService assistantConfigService, ToolRegistry toolRegistry) {
        this.assistantConfigService = assistantConfigService;
        this.toolRegistry = toolRegistry;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<AssistantResponse> list(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return assistantConfigService.listResponses(includeInactive);
    }

    @GetMapping(value = "/catalog/tools", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ToolCatalogEntryDto> toolCatalog() {
        return toolRegistry.listAll().stream()
                .sorted((a, b) -> a.name().compareTo(b.name()))
                .map(AssistantController::toCatalogEntry)
                .toList();
    }

    @GetMapping(value = "/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
    public AssistantResponse get(@PathVariable String code) {
        return assistantConfigService.getByCode(code);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AssistantResponse create(@Valid @RequestBody AssistantRequest request) {
        return assistantConfigService.create(request);
    }

    @PutMapping(value = "/{code}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AssistantResponse update(@PathVariable String code, @Valid @RequestBody AssistantRequest request) {
        return assistantConfigService.update(code, request);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) {
        assistantConfigService.delete(code);
    }

    private static ToolCatalogEntryDto toCatalogEntry(AgentTool tool) {
        return new ToolCatalogEntryDto(tool.name(), tool.description(), tool.kind());
    }
}
