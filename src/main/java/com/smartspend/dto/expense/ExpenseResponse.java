package com.smartspend.dto.expense;

import com.smartspend.model.entity.Expense;
import com.smartspend.model.enums.Category;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO returned by expense endpoints.
 * Separates the API response from the internal entity.
 */
@Data
@Builder
public class ExpenseResponse {

    private Long id;
    private String title;
    private BigDecimal amount;
    private Category category;
    private String categoryDisplayName;
    private String categoryEmoji;
    private LocalDate expenseDate;
    private String notes;
    private LocalDateTime createdAt;

    /**
     * Convenience factory method to convert an Expense entity to this DTO.
     */
    public static ExpenseResponse from(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .amount(expense.getAmount())
                .category(expense.getCategory())
                .categoryDisplayName(expense.getCategory().getDisplayName())
                .categoryEmoji(expense.getCategory().getEmoji())
                .expenseDate(expense.getExpenseDate())
                .notes(expense.getNotes())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}
