package com.agentapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import com.agentapi.config.DotenvLoader;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AgentApiApplication {

    static {
        DotenvLoader.init();
    }

    public static void main(String[] args) {
        SpringApplication.run(AgentApiApplication.class, args);
    }
}
