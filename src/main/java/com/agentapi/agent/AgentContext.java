package com.agentapi.agent;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import com.agentapi.assistant.AssistantType;

public class AgentContext {

    private final UUID tenantId;
    private final UUID userId;
    private final String phoneNumber;
    private final AssistantType assistant;
    private final Set<String> permissions;

    private AgentContext(Builder builder) {
        this.tenantId = builder.tenantId;
        this.userId = builder.userId;
        this.phoneNumber = builder.phoneNumber;
        this.assistant = builder.assistant;
        this.permissions = builder.permissions != null
                ? Set.copyOf(builder.permissions)
                : Collections.emptySet();
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public AssistantType getAssistant() {
        return assistant;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID tenantId;
        private UUID userId;
        private String phoneNumber;
        private AssistantType assistant;
        private Set<String> permissions;

        public Builder tenantId(UUID tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder assistant(AssistantType assistant) {
            this.assistant = assistant;
            return this;
        }

        public Builder permissions(Set<String> permissions) {
            this.permissions = permissions;
            return this;
        }

        public AgentContext build() {
            return new AgentContext(this);
        }
    }
}
