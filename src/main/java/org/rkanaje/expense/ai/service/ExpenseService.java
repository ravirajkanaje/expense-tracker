package org.rkanaje.expense.ai.service;

import org.rkanaje.expense.ai.model.Expense;

import java.util.List;

public interface ExpenseService {
    List<Expense> getExpensesByYear(int year);
}
