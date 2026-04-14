package com.smartspend.dto.expense;

import com.smartspend.model.enums.Category;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating or updating an expense.
 */
@Data
public class ExpenseRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 200, message = "Title must be 2–200 characters")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Date is required")
    private LocalDate expenseDate;

    private String notes;
}
