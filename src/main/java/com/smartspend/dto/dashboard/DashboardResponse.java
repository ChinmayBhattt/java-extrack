package com.smartspend.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO returned by GET /api/dashboard.
 * Contains all data needed to render the analytics dashboard in one request.
 */
@Data
@Builder
public class DashboardResponse {

    // ── User Info ─────────────────────────────────────────────────────────
    private String userName;
    private BigDecimal salary;

    // ── This Month Summary ────────────────────────────────────────────────
    private BigDecimal totalThisMonth;
    private BigDecimal savingsThisMonth;        // salary - totalThisMonth
    private int currentMonth;
    private int currentYear;

    // ── All-Time ──────────────────────────────────────────────────────────
    private BigDecimal totalAllTime;
    private long totalExpenseCount;

    // ── Category Breakdown (this month) ──────────────────────────────────
    private Map<String, BigDecimal> categoryBreakdown;  // key = Category.name()
    private Map<String, String>     categoryLabels;     // key = Category.name(), value = displayName + emoji

    // ── Monthly Trend (last 6 months) ────────────────────────────────────
    private List<String>     trendLabels;    // e.g. ["Nov", "Dec", "Jan", …]
    private List<BigDecimal> trendAmounts;   // matching amounts

    // ── Budget Warnings ───────────────────────────────────────────────────
    private int budgetWarningsCount;

    // ── Expense Assistant Advice ──────────────────────────────────────────
    private String assistantAdvice;
    private String assistantAdviceType;   // "success" | "warning" | "danger"
}
