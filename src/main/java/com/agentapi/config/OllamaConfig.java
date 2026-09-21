package com.agentapi.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import com.agentapi.llm.LlmClient;
import com.agentapi.llm.OllamaClient;

@Configuration
public class OllamaConfig {

    private static final Duration PROBE_TIMEOUT = Duration.ofSeconds(5);

    @Bean
    RestClient ollamaRestClient(OllamaProperties properties) {
        Duration timeout = properties.getTimeout();
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(timeout)
                .withReadTimeout(timeout);
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .build();
    }

    /** Timeout curto para health/diagnóstico — não bloquear /api/agent/status se o Ollama estiver off. */
    @Bean
    RestClient ollamaProbeRestClient(OllamaProperties properties) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(PROBE_TIMEOUT)
                .withReadTimeout(PROBE_TIMEOUT);
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "llm.provider", havingValue = "ollama")
    LlmClient ollamaLlmClient(RestClient ollamaRestClient, OllamaProperties properties) {
        return new OllamaClient(ollamaRestClient, properties);
    }
}
