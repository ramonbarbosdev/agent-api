package com.agentapi.gateway.project;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agentapi.web.ProjectResponse;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final PathAuthorizationService pathAuthorizationService;

    public ProjectController(PathAuthorizationService pathAuthorizationService) {
        this.pathAuthorizationService = pathAuthorizationService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ProjectResponse> list() {
        return pathAuthorizationService.projectRegistry().stream()
                .filter(ProjectDefinition::enabled)
                .map(p -> new ProjectResponse(p.id(), p.name(), p.enabled()))
                .toList();
    }
}
