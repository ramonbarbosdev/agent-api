package com.agentapi.gateway.audit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.agentapi.config.GatewayProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GatewayAuditService {

    private static final Logger log = LoggerFactory.getLogger(GatewayAuditService.class);

    private final GatewayProperties gatewayProperties;
    private final ObjectMapper objectMapper;

    public GatewayAuditService(GatewayProperties gatewayProperties, ObjectMapper objectMapper) {
        this.gatewayProperties = gatewayProperties;
        this.objectMapper = objectMapper;
    }

    public void record(
            UUID userId,
            UUID sessionId,
            String project,
            String agent,
            String operation,
            String status) {
        try {
            Path dir = Path.of(gatewayProperties.getAuditPath());
            Files.createDirectories(dir);
            Path file = dir.resolve("agent-api-" + LocalDate.now() + ".jsonl");
            var entry = new AuditEntry(Instant.now().toString(), userId.toString(), sessionId.toString(), project, agent, operation, status);
            String line = objectMapper.writeValueAsString(entry) + System.lineSeparator();
            Files.writeString(file, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            log.warn("Audit write failed: {}", ex.getMessage());
        }
    }

    private record AuditEntry(
            String timestamp,
            String userId,
            String sessionId,
            String project,
            String agent,
            String operation,
            String status) {
    }
}
