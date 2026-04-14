package com.smartspend.dto.budget;

import com.smartspend.model.enums.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO for creating or updating a budget limit.
 */
@Data
public class BudgetRequest {

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Limit amount is required")
    @DecimalMin(value = "1.00", message = "Budget limit must be at least 1.00")
    private BigDecimal limitAmount;
}
