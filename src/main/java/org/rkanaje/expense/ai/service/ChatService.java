package org.rkanaje.expense.ai.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ChatService {

    private final ResourceLoader resourceLoader;

    protected ChatService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    protected String getSystemPrompt(String promptFile) throws IOException {
        Resource resource = resourceLoader.getResource(promptFile);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
