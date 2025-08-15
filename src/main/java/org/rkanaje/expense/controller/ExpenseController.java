package org.rkanaje.expense.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rkanaje.expense.ai.model.Expense;
import org.rkanaje.expense.ai.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/v1/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<Expense>> getExpenses(
            @RequestParam(required = false) Integer year) {
        try {
            int targetYear = (year != null) ? year : Year.now().getValue();
            List<Expense> expenses = expenseService.getExpensesByYear(targetYear);
            return ResponseEntity.ok(expenses);
        } catch (Exception e) {
            log.error("Error fetching expenses: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
