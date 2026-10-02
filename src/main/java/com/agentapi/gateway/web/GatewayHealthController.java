package com.agentapi.gateway.web;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agentapi.config.GatewayProperties;
import com.agentapi.gateway.sidecar.CursorSidecarClient;
import com.agentapi.web.HealthResponse;

@RestController
public class GatewayHealthController {

    private final GatewayProperties gatewayProperties;
    private final CursorSidecarClient sidecarClient;

    public GatewayHealthController(GatewayProperties gatewayProperties, CursorSidecarClient sidecarClient) {
        this.gatewayProperties = gatewayProperties;
        this.sidecarClient = sidecarClient;
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public HealthResponse health() {
        return new HealthResponse("UP");
    }

    @GetMapping(value = "/health/ready", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> ready() {
        Map<String, Object> body = new LinkedHashMap<>();
        boolean iaOk = Files.isDirectory(Path.of(gatewayProperties.getIaPath()));
        boolean sidecarOk = sidecarClient.isHealthy();
        body.put("status", iaOk && sidecarOk ? "UP" : "DOWN");
        body.put("iaPath", iaOk);
        body.put("sidecar", sidecarOk);
        return body;
    }
}
