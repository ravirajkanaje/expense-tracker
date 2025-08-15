package org.rkanaje.expense.ai.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class Expense {

    private double amount;
    private String topic;
    private String date;

}
