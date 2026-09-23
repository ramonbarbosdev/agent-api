package com.agentapi.agent;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import com.agentapi.assistant.AssistantType;

public class AgentContext {

    private final UUID userId;
    private final String phoneNumber;
    private final AssistantType assistant;
    private final UUID conversationId;
    private final Set<String> permissions;

    private AgentContext(Builder builder) {
        this.userId = builder.userId;
        this.phoneNumber = builder.phoneNumber;
        this.assistant = builder.assistant;
        this.conversationId = builder.conversationId;
        this.permissions = builder.permissions != null
                ? Set.copyOf(builder.permissions)
                : Collections.emptySet();
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

    public UUID getConversationId() {
        return conversationId;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private UUID userId;
        private String phoneNumber;
        private AssistantType assistant;
        private UUID conversationId;
        private Set<String> permissions;

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

        public Builder conversationId(UUID conversationId) {
            this.conversationId = conversationId;
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
