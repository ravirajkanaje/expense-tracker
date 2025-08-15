package org.rkanaje.expense.ai.tool;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rkanaje.expense.ai.model.Expense;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A Spring AI Tool for updating a Google Sheet with expense data.
 * This tool can be used by AI models to interact with Google Sheets.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleSheetsTool {

    private static final String SHEET_PREFIX = "Expense_";
    private static final String RANGE = "A:C"; // Columns: Date, Amount, Description
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final List<Object> HEADER_ROW = List.of("Date", "Amount", "Description");

    private final Sheets sheetsService;
    private final String spreadsheetId;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Tool(name = "updateExpensesByYear",
            description = "Manage expenses in the Google Sheet, organized by year. Can add/update and delete expenses in a single operation.")
    public String updateExpensesByYear(
            @ToolParam(description = "List of expenses to add or update") List<Expense> expensesToAddOrUpdate,
            @ToolParam(description = "List of expenses to delete (only date and topic are used for matching)") List<Expense> expensesToDelete) {

        Map<String, String> results = new LinkedHashMap<>();

        // Handle additions/updates
        if (expensesToAddOrUpdate != null && !expensesToAddOrUpdate.isEmpty()) {
            // Group expenses by year
            Map<String, List<Expense>> expensesByYear = expensesToAddOrUpdate.stream()
                    .collect(Collectors.groupingBy(
                            expense -> Year.from(LocalDate.parse(expense.getDate())).toString()
                    ));

            for (Map.Entry<String, List<Expense>> entry : expensesByYear.entrySet()) {
                String year = entry.getKey();
                String sheetName = SHEET_PREFIX + year;

                try {
                    ensureSheetExists(sheetName);
                    String result = updateYearSheet(sheetName, entry.getValue());
                    results.put(year, "Updated: " + result);
                } catch (Exception e) {
                    String error = String.format("Failed to update sheet for year %s: %s", year, e.getMessage());
                    log.error(error, e);
                    results.put(year, "Error: " + error);
                }
            }
        }

        // Handle deletions
        if (expensesToDelete != null && !expensesToDelete.isEmpty()) {
            // Group deletions by year
            Map<String, List<Expense>> deletesByYear = expensesToDelete.stream()
                    .collect(Collectors.groupingBy(
                            expense -> Year.from(LocalDate.parse(expense.getDate())).toString()
                    ));

            for (Map.Entry<String, List<Expense>> entry : deletesByYear.entrySet()) {
                String year = entry.getKey();
                String sheetName = SHEET_PREFIX + year;

                try {
                    int deletedCount = deleteExpensesFromSheet(sheetName, entry.getValue());
                    String result = String.format("Deleted %d expense(s)", deletedCount);
                    if (results.containsKey(year)) {
                        results.put(year, results.get(year) + " | " + result);
                    } else {
                        results.put(year, result);
                    }
                } catch (Exception e) {
                    String error = String.format("Failed to delete expenses from sheet %s: %s", sheetName, e.getMessage());
                    log.error(error, e);
                    results.put(year, (results.getOrDefault(year, "") + " | Error: " + error).trim());
                }
            }
        }

        if (results.isEmpty()) {
            return "No operations performed. No expenses provided for adding/updating or deleting.";
        }

        return results.entrySet().stream()
                .map(e -> "Year " + e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Deletes expenses from the specified sheet that match the given expense dates and topics.
     *
     * @param sheetName        The name of the sheet to delete from
     * @param expensesToDelete List of expenses to delete (only date and topic are used for matching)
     * @return Number of expenses deleted
     */
    private int deleteExpensesFromSheet(String sheetName, List<Expense> expensesToDelete) throws IOException {
        if (expensesToDelete == null || expensesToDelete.isEmpty()) {
            return 0;
        }

        // Get all values from the sheet
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, sheetName + "!" + RANGE)
                .execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            return 0;
        }

        // Create a set of (date, topic) pairs to delete for faster lookup
        Set<String> deleteKeys = expensesToDelete.stream()
                .map(e -> e.getDate() + "|" + e.getTopic().toLowerCase())
                .collect(Collectors.toSet());

        // Keep only rows that don't match any of the delete criteria
        List<List<Object>> updatedValues = new ArrayList<>();
        updatedValues.add(values.get(0)); // Keep header

        int deletedCount = 0;
        for (int i = 1; i < values.size(); i++) {
            List<Object> row = values.get(i);
            if (row.size() >= 3) { // Ensure we have date, amount, and topic
                String date = row.get(0).toString();
                String topic = row.get(2).toString().toLowerCase();
                String rowKey = date + "|" + topic;

                if (!deleteKeys.contains(rowKey)) {
                    updatedValues.add(row);
                } else {
                    deletedCount++;
                }
            } else {
                updatedValues.add(row); // Keep malformed rows
            }
        }

        // Only update if there were deletions
        if (deletedCount > 0) {
            // Clear the sheet and write back the remaining rows
            sheetsService.spreadsheets().values()
                    .clear(spreadsheetId, sheetName + "!" + RANGE, new ClearValuesRequest())
                    .execute();

            if (!updatedValues.isEmpty()) {
                ValueRange body = new ValueRange().setValues(updatedValues);
                sheetsService.spreadsheets().values()
                        .update(spreadsheetId, sheetName + "!" + RANGE, body)
                        .setValueInputOption("USER_ENTERED")
                        .execute();
            }
        }

        return deletedCount;
    }

    private void ensureSheetExists(String sheetName) throws IOException {
        // Get all sheets
        List<Sheet> sheets = sheetsService.spreadsheets()
                .get(spreadsheetId)
                .execute()
                .getSheets();

        // Check if sheet exists
        boolean sheetExists = sheets.stream()
                .anyMatch(sheet -> sheet.getProperties().getTitle().equals(sheetName));

        if (!sheetExists) {
            // Create new sheet
            AddSheetRequest addSheetRequest = new AddSheetRequest()
                    .setProperties(new SheetProperties().setTitle(sheetName));

            BatchUpdateSpreadsheetRequest batchUpdateRequest = new BatchUpdateSpreadsheetRequest()
                    .setRequests(Collections.singletonList(
                            new Request().setAddSheet(new AddSheetRequest()
                                    .setProperties(new SheetProperties().setTitle(sheetName)))));

            sheetsService.spreadsheets()
                    .batchUpdate(spreadsheetId, batchUpdateRequest)
                    .execute();

            // Add header row to the new sheet
            ValueRange headerBody = new ValueRange()
                    .setValues(Collections.singletonList(HEADER_ROW));

            sheetsService.spreadsheets().values()
                    .update(spreadsheetId, sheetName + "!A1", headerBody)
                    .setValueInputOption("USER_ENTERED")
                    .execute();

            log.info("Created new sheet: {}", sheetName);
        }
    }

    private String updateYearSheet(String sheetName, List<Expense> expenses) throws IOException {
        // Get the current data to find the next empty row
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, sheetName + "!" + RANGE)
                .execute();

        List<List<Object>> values = response.getValues();
        int nextRow = (values != null && !values.isEmpty()) ? values.size() + 1 : 2; // +1 for header row

        // Prepare the new rows of data
        List<List<Object>> newRows = new ArrayList<>();
        for (Expense expense : expenses) {
            newRows.add(createRowData(expense));
        }

        // Update the sheet
        ValueRange body = new ValueRange()
                .setValues(newRows);

        String updateRange = String.format("%s!A%d:C", sheetName, nextRow);
        UpdateValuesResponse result = sheetsService.spreadsheets().values()
                .update(spreadsheetId, updateRange, body)
                .setValueInputOption("USER_ENTERED")
                .execute();

        log.info("Updated {} rows in sheet {}", result.getUpdatedRows(), sheetName);
        return String.format("Added %d expenses to %s", expenses.size(), sheetName);
    }

    /**
     * Creates a single row of data from an Expense object
     */
    private List<Object> createRowData(Expense expense) {
        List<Object> row = new ArrayList<>();
        // Format: Date, Amount, Description
        row.add(expense.getDate() != null ? expense.getDate() : LocalDate.now().format(DATE_FORMATTER));
        row.add(expense.getAmount());
        row.add(expense.getTopic());
        return row;
    }

    /**
     * Retrieves expenses matching the specified criteria.
     * Year is required. If month is provided, year must be provided.
     * If day is provided, both year and month must be provided.
     *
     * @param year   The year to search (e.g., 2025) - required
     * @param month  The month to search (1-12), requires year - optional
     * @param day    The day of month to search (1-31), requires year and month - optional
     * @param topic  The topic to match (case-insensitive), or null to match any topic - optional
     * @param amount The amount to match, or null to match any amount - optional
     * @return List of matching expenses
     * @throws IllegalArgumentException if parameters are not provided in the correct combination
     */
    @Tool(name = "getExpenses",
            description = "Search for expenses matching the specified criteria. Year is required. " +
                    "If month is provided, year must be provided. " +
                    "If day is provided, both year and month must be provided.")
    public List<Expense> getExpenses(
            @ToolParam(description = "Year to search (e.g., 2025)", required = true) Integer year,
            @ToolParam(description = "Month to search (1-12), requires year", required = false) Integer month,
            @ToolParam(description = "Day of month to search (1-31), requires year and month", required = false) Integer day,
            @ToolParam(description = "Topic to match (case-insensitive partial match)", required = false) String topic,
            @ToolParam(description = "Exact amount to match", required = false) Double amount) {

        // Validate parameter combinations
        if (month != null && year == null) {
            throw new IllegalArgumentException("Year must be provided when month is specified");
        }
        if (day != null && (year == null || month == null)) {
            throw new IllegalArgumentException("Both year and month must be provided when day is specified");
        }

        // Get the sheet for the specified year
        String sheetName = SHEET_PREFIX + year;
        List<Expense> matchingExpenses = new ArrayList<>();

        try {
            // Check if the sheet exists
            List<Sheet> sheets = sheetsService.spreadsheets()
                    .get(spreadsheetId)
                    .execute()
                    .getSheets();

            if (sheets.stream().noneMatch(s -> s.getProperties().getTitle().equals(sheetName))) {
                return Collections.emptyList(); // No data for this year
            }

            // Get all values from the sheet
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, sheetName + "!" + RANGE)
                    .execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.size() <= 1) {
                return Collections.emptyList(); // Skip empty sheets or sheets with only header
            }

            // Process each row (skip header)
            for (int i = 1; i < values.size(); i++) {
                List<Object> row = values.get(i);
                if (row.size() < 3) {
                    continue; // Skip malformed rows
                }

                try {
                    String rowDateStr = row.get(0).toString();
                    String rowAmountStr = row.get(1).toString();
                    String rowTopic = row.get(2).toString();

                    // Parse amount
                    double rowAmount;
                    try {
                        rowAmount = Double.parseDouble(rowAmountStr);
                    } catch (NumberFormatException e) {
                        continue; // Skip rows with invalid amounts
                    }

                    // Parse date
                    LocalDate rowDate;
                    try {
                        rowDate = LocalDate.parse(rowDateStr, DATE_FORMAT);
                    } catch (Exception e) {
                        continue; // Skip rows with invalid dates
                    }

                    // Check month filter if provided
                    if (month != null && rowDate.getMonthValue() != month) {
                        continue;
                    }

                    // Check day filter if provided
                    if (day != null && rowDate.getDayOfMonth() != day) {
                        continue;
                    }

                    // Check amount filter if provided
                    if (amount != null && Math.abs(rowAmount - amount) > 0.001) {
                        continue;
                    }

                    // Check topic filter if provided
                    if (topic != null && !rowTopic.toLowerCase().contains(topic.toLowerCase())) {
                        continue;
                    }

                    // If we get here, all filters passed
                    matchingExpenses.add(new Expense(rowAmount, rowTopic, rowDate.format(DATE_FORMAT)));
                } catch (Exception e) {
                    log.warn("Error processing row {} in sheet {}: {}", i, sheetName, e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Error accessing Google Sheets: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve expenses: " + e.getMessage(), e);
        }
        return matchingExpenses;
    }
}
