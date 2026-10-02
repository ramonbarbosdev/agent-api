package com.agentapi.gateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.agentapi.config.GatewayProperties;
import com.agentapi.exception.ApiException;
import com.agentapi.gateway.project.PathAuthorizationService;

class PathAuthorizationServiceTest {

    @Test
    void rejectsWhenNoDefaultAndNoProjectId() {
        GatewayProperties props = new GatewayProperties();
        props.setProjects(List.of());
        PathAuthorizationService service = new PathAuthorizationService(props);
        assertThatThrownBy(() -> service.resolveWorkspacePath(null))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void usesDefaultProjectPath() {
        GatewayProperties props = new GatewayProperties();
        props.setDefaultProjectPath(System.getProperty("java.io.tmpdir"));
        PathAuthorizationService service = new PathAuthorizationService(props);
        Path path = service.resolveWorkspacePath(null);
        assertThat(path).isNotNull();
    }

    @Test
    void rejectsUnknownProjectWhenNoDefault() {
        GatewayProperties props = new GatewayProperties();
        props.setProjects(List.of());
        PathAuthorizationService service = new PathAuthorizationService(props);
        assertThatThrownBy(() -> service.resolveAuthorizedProjectPath("missing"))
                .isInstanceOf(ApiException.class);
    }
}
