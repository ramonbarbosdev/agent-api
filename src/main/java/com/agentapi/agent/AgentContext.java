package com.agentapi.agent;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class AgentContext {

    private final UUID userId;
    private final String phoneNumber;
    private final String assistantCode;
    private final UUID conversationId;
    private final Set<String> permissions;

    private AgentContext(Builder builder) {
        this.userId = builder.userId;
        this.phoneNumber = builder.phoneNumber;
        this.assistantCode = builder.assistantCode;
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

    public String getAssistantCode() {
        return assistantCode;
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
        private String assistantCode;
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

        public Builder assistantCode(String assistantCode) {
            this.assistantCode = assistantCode;
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
