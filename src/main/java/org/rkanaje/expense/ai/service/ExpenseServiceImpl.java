package org.rkanaje.expense.ai.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rkanaje.expense.ai.model.Expense;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private static final String SHEET_PREFIX = "Expense_";
    private static final String RANGE = "A:C"; // Columns: Date, Amount, Description
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Sheets sheetsService;
    private final String spreadsheetId;

    @Override
    public List<Expense> getExpensesByYear(int year) {
        String sheetName = SHEET_PREFIX + year;
        List<Expense> expenses = new ArrayList<>();

        try {
            // First, check if the sheet exists
            boolean sheetExists = sheetsService.spreadsheets().get(spreadsheetId)
                    .execute()
                    .getSheets()
                    .stream()
                    .anyMatch(sheet -> sheet.getProperties().getTitle().equals(sheetName));

            if (!sheetExists) {
                log.info("Sheet '{}' does not exist, returning empty list", sheetName);
                return expenses;
            }

            // Get all values from the sheet
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, sheetName + "!" + RANGE)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                log.info("No data found in sheet: {}", sheetName);
                return expenses;
            }

            // Skip header row and process each row as an expense
            for (int i = 1; i < values.size(); i++) {
                List<Object> row = values.get(i);
                if (row.size() >= 3) { // Ensure we have all required columns
                    try {
                        Expense expense = Expense.builder()
                                .date((String) row.get(0))
                                .amount(Double.parseDouble(row.get(1).toString()))
                                .topic((String) row.get(2))
                                .build();
                        expenses.add(expense);
                    } catch (Exception e) {
                        log.warn("Error parsing expense row {}: {}", i, row, e);
                    }
                }
            }

            // Sort expenses by date in ascending order
            expenses.sort((e1, e2) -> e1.getDate().compareTo(e2.getDate()));

        } catch (IOException e) {
            log.warn("Error accessing Google Sheets for year {}: {}", year, e.getMessage());
            // Return empty list instead of throwing exception
            return new ArrayList<>();
        }
        return expenses;
    }
}
