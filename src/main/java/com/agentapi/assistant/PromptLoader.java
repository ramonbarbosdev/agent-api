package com.agentapi.assistant;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class PromptLoader {

    public String load(String classpathLocation) {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        try (InputStream in = resource.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível carregar o prompt: " + classpathLocation, e);
        }
    }
}
