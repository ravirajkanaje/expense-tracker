package org.rkanaje.expense.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.rkanaje.expense.ai.model.Expense;
import org.rkanaje.expense.ai.tool.DateTool;
import org.rkanaje.expense.ai.tool.GoogleSheetsTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ExpenseChatService extends ChatService {

    private final ChatClient chatClient;
    private final GoogleSheetsTool sheetsUpdateTool;
    private final DateTool dateTool;

    @Autowired
    public ExpenseChatService(ChatClient chatClient,
                              DateTool dateTool,
                              GoogleSheetsTool sheetsUpdateTool,
                              ResourceLoader resourceLoader) {
        super(resourceLoader);
        this.chatClient = chatClient;
        this.sheetsUpdateTool = sheetsUpdateTool;
        this.dateTool = dateTool;
    }

    public List<Expense> parseChatMessage(String message) {
        try {
            final String systemPrompt = getSystemPrompt("classpath:prompts/system_expense.template");
            // Call the chat client with the system prompt and user message
            // Using response binding to directly parse into List<Expense>
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(message)
                    .call()
                    .entity(new ParameterizedTypeReference<List<Expense>>() {
                    });
        } catch (Exception e) {
            log.error("Error processing chat message", e);
            throw new RuntimeException("Failed to process expense information. Please try again.", e);
        }
    }

    public String processChatMessage(String message) {
        try {
            final String systemPrompt = getSystemPrompt("classpath:prompts/system_expense_with_tools.template");
            
            // Use the chat client with tool invocation
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(message)
                    .tools(Arrays.asList(sheetsUpdateTool, dateTool).toArray())
                    .call()
                    .content().trim();
                    
        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage(), e);
            return "I'm sorry, I encountered an error while processing your request. Please try again or rephrase your message.";
        }
    }

}
