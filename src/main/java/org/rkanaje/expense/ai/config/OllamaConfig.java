package org.rkanaje.expense.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ollama")
public class OllamaConfig {

    private String host;
    private Chat chat;

    @Getter
    @Setter
    public static class Chat {
        private String model;
        private Options options;
    }

    @Getter
    @Setter
    public static class Options {
        private double temperature;
        private int numCtx;
    }

}
