package com.agentapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import org.springframework.beans.factory.ObjectProvider;

import com.agentapi.agent.AgentChatWebSocketHandler;
import com.agentapi.auth.JwtWebSocketInterceptor;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final AgentChatWebSocketHandler agentChatWebSocketHandler;
    private final ObjectProvider<JwtWebSocketInterceptor> jwtWebSocketInterceptor;
    private final AuthProperties authProperties;

    public WebSocketConfig(
            AgentChatWebSocketHandler agentChatWebSocketHandler,
            ObjectProvider<JwtWebSocketInterceptor> jwtWebSocketInterceptor,
            AuthProperties authProperties) {
        this.agentChatWebSocketHandler = agentChatWebSocketHandler;
        this.jwtWebSocketInterceptor = jwtWebSocketInterceptor;
        this.authProperties = authProperties;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        var registration = registry.addHandler(agentChatWebSocketHandler, "/ws/agent/chat")
                .setAllowedOrigins(AgentCorsOrigins.asArray());
        if (authProperties.isEnabled()) {
            jwtWebSocketInterceptor.ifAvailable(registration::addInterceptors);
        }
    }
}
