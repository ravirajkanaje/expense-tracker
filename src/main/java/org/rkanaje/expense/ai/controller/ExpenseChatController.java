package org.rkanaje.expense.ai.controller;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.rkanaje.expense.ai.model.ChatInput;
import org.rkanaje.expense.ai.model.ChatOutput;
import org.rkanaje.expense.ai.model.Expense;
import org.rkanaje.expense.ai.service.ExpenseChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/v1/expense")
@Slf4j
public class ExpenseChatController {

    private final ExpenseChatService expenseChatService;

    @Autowired
    public ExpenseChatController(ExpenseChatService expenseChatService) {
        this.expenseChatService = expenseChatService;
    }


    @PostMapping(path = "/chat",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatOutput> chatPost(@RequestBody @NonNull ChatInput input) {
        try {
            return ResponseEntity.ok().body(ChatOutput.builder().message(expenseChatService.processChatMessage(input.getMessage()))
                    .build());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping(path = "/parse",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Expense>> parseChat(@RequestBody @NonNull ChatInput input) {
        try {
            final List<Expense> expenses = expenseChatService.parseChatMessage(input.getMessage());
            return ResponseEntity.ok().body(expenses);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
