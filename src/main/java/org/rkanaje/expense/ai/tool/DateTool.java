package org.rkanaje.expense.ai.tool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A Spring AI Tool for updating a Google Sheet with expense data.
 * This tool can be used by AI models to interact with Google Sheets.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DateTool {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Tool(name = "getDate",
            description = "Get the current date. Returns a string in the format 'yyyy-MM-dd'.")
    public String getDate() {
        LocalDate currentDate = LocalDate.now();
        return currentDate.format(DATE_FORMATTER);
    }


}
