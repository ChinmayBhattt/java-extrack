package com.smartspend.dto.budget;

import com.smartspend.model.enums.Category;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO representing a single budget entry with its current spending status.
 */
@Data
@Builder
public class BudgetResponse {

    private Long id;
    private Category category;
    private String categoryDisplayName;
    private String categoryEmoji;
    private BigDecimal limitAmount;
    private BigDecimal amountSpent;
    private BigDecimal remaining;
    private double progressPercent;   // 0-100 (may exceed 100 if over budget)
    private boolean overLimit;
}
