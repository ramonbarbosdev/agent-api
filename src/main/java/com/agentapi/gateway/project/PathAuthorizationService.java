package com.agentapi.gateway.project;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.springframework.stereotype.Service;

import com.agentapi.config.GatewayProperties;
import com.agentapi.exception.ApiException;
import com.agentapi.exception.ErrorCode;

@Service
public class PathAuthorizationService {

    public static final String DEFAULT_WORKSPACE_ID = "workspace";

    private final GatewayProperties gatewayProperties;

    public PathAuthorizationService(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    public void ensureWorkspaceConfigured() {
        resolveWorkspacePath(null);
    }

    public Path resolveWorkspacePath(String projectId) {
        String defaultPath = gatewayProperties.getDefaultProjectPath();
        if (defaultPath != null && !defaultPath.isBlank()) {
            return validatePath(Path.of(defaultPath.trim()));
        }
        if (projectId == null || projectId.isBlank()) {
            throw new ApiException(
                    ErrorCode.PROJECT_NOT_FOUND,
                    "Configure AGENT_GATEWAY_DEFAULT_PROJECT_PATH no servidor ou informe projectId.");
        }
        return resolveAuthorizedProjectPath(projectId.trim());
    }

    public Path resolveAuthorizedProjectPath(String projectId) {
        ProjectDefinition project = projectRegistry().stream()
                .filter(p -> p.id().equals(projectId))
                .findFirst()
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND, "Projeto não encontrado."));
        if (!project.enabled()) {
            throw new ApiException(ErrorCode.PROJECT_NOT_ALLOWED, "Projeto desabilitado.");
        }
        return validatePath(Path.of(project.path()));
    }

    private Path validatePath(Path path) {
        Path normalized = path.normalize().toAbsolutePath();
        validateUnderAllowlist(normalized);
        return normalized;
    }

    private void validateUnderAllowlist(Path path) {
        List<String> roots = gatewayProperties.getProjectRootAllowlist();
        if (roots == null || roots.isEmpty()) {
            return;
        }
        try {
            Path real = path.toRealPath();
            for (String root : roots) {
                Path allowed = Path.of(root).normalize().toAbsolutePath().toRealPath();
                if (real.startsWith(allowed)) {
                    return;
                }
            }
        } catch (IOException ex) {
            throw new ApiException(ErrorCode.PROJECT_NOT_ALLOWED, "Caminho do projeto inválido.");
        }
        throw new ApiException(ErrorCode.PROJECT_NOT_ALLOWED, "O projeto informado não está autorizado.");
    }

    public List<ProjectDefinition> projectRegistry() {
        String defaultPath = gatewayProperties.getDefaultProjectPath();
        if (defaultPath != null && !defaultPath.isBlank()) {
            return List.of(new ProjectDefinition(
                    DEFAULT_WORKSPACE_ID, "Workspace Cursor", defaultPath.trim(), true));
        }
        return gatewayProperties.getProjects().stream()
                .map(e -> new ProjectDefinition(e.getId(), e.getName(), e.getPath(), e.isEnabled()))
                .toList();
    }
}
