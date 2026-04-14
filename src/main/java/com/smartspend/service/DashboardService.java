package com.smartspend.service;

import com.smartspend.dto.dashboard.DashboardResponse;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.model.entity.User;
import com.smartspend.model.enums.Category;
import com.smartspend.repository.BudgetRepository;
import com.smartspend.repository.ExpenseRepository;
import com.smartspend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Builds the dashboard analytics summary for the authenticated user.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository  budgetRepository;
    private final UserRepository    userRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        int year  = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        Long uid  = user.getId();

        // ── Totals ────────────────────────────────────────────────────────
        BigDecimal totalThisMonth = expenseRepository.sumByUserAndMonth(uid, year, month);
        if (totalThisMonth == null) totalThisMonth = BigDecimal.ZERO;

        BigDecimal totalAllTime = expenseRepository.sumAllByUser(uid);
        if (totalAllTime == null) totalAllTime = BigDecimal.ZERO;

        BigDecimal salary = user.getSalary() != null ? user.getSalary() : BigDecimal.ZERO;
        BigDecimal savings = salary.subtract(totalThisMonth);

        long count = expenseRepository.findByUserIdOrderByExpenseDateDesc(uid).size();

        // ── Category Breakdown ────────────────────────────────────────────
        List<Object[]> breakdown = expenseRepository.getCategoryBreakdown(uid, year, month);
        Map<String, BigDecimal> categoryBreakdown = new LinkedHashMap<>();
        Map<String, String>     categoryLabels    = new LinkedHashMap<>();

        for (Object[] row : breakdown) {
            Category cat    = (Category) row[0];
            BigDecimal amt  = (BigDecimal) row[1];
            categoryBreakdown.put(cat.name(), amt);
            categoryLabels.put(cat.name(), cat.getEmoji() + " " + cat.getDisplayName());
        }

        // ── Monthly Trend (last 6 months) ─────────────────────────────────
        List<Object[]> trend = expenseRepository.getMonthlyTrend(uid);
        List<String>     trendLabels  = new ArrayList<>();
        List<BigDecimal> trendAmounts = new ArrayList<>();

        int count2 = 0;
        for (Object[] row : trend) {
            if (count2 >= 6) break;
            int    y = (int) row[0];
            int    m = (int) row[1];
            BigDecimal a = (BigDecimal) row[2];
            String label = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + y;
            trendLabels.add(label);
            trendAmounts.add(a);
            count2++;
        }
        // Reverse so oldest is first (left on chart)
        Collections.reverse(trendLabels);
        Collections.reverse(trendAmounts);

        // ── Budget Warnings ───────────────────────────────────────────────
        long warningCount = budgetRepository.findByUserId(uid).stream()
            .filter(b -> {
                BigDecimal spent = expenseRepository
                    .getSpendingByCategory(uid, year, month)
                    .stream()
                    .filter(r -> r[0].equals(b.getCategory()))
                    .map(r -> (BigDecimal) r[1])
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
                return spent.compareTo(b.getLimitAmount()) > 0;
            })
            .count();

        // ── Expense Assistant Advice ──────────────────────────────────────
        String[] advice = generateAdvice(salary, totalThisMonth);

        return DashboardResponse.builder()
                .userName(user.getName())
                .salary(salary)
                .totalThisMonth(totalThisMonth)
                .savingsThisMonth(savings)
                .currentMonth(month)
                .currentYear(year)
                .totalAllTime(totalAllTime)
                .totalExpenseCount(count)
                .categoryBreakdown(categoryBreakdown)
                .categoryLabels(categoryLabels)
                .trendLabels(trendLabels)
                .trendAmounts(trendAmounts)
                .budgetWarningsCount((int) warningCount)
                .assistantAdvice(advice[0])
                .assistantAdviceType(advice[1])
                .build();
    }

    // ── Private: Expense Assistant Logic ──────────────────────────────────

    private String[] generateAdvice(BigDecimal salary, BigDecimal spent) {
        if (salary == null || salary.compareTo(BigDecimal.ZERO) == 0) {
            return new String[]{
                "Set your monthly salary in your profile to unlock personalized savings advice!",
                "info"
            };
        }

        double spentPct = spent.divide(salary, 4, RoundingMode.HALF_UP)
                               .multiply(BigDecimal.valueOf(100))
                               .doubleValue();

        if (spentPct <= 50) {
            return new String[]{
                String.format("Excellent! You have spent only %.1f%% of your salary. Keep saving! 🎉", spentPct),
                "success"
            };
        } else if (spentPct <= 75) {
            return new String[]{
                String.format("Good going — %.1f%% of salary spent. You're on track, but watch discretionary costs.", spentPct),
                "warning"
            };
        } else if (spentPct <= 100) {
            return new String[]{
                String.format("⚠️ Alert: You've used %.1f%% of your salary this month. Consider reducing non-essential spending.", spentPct),
                "danger"
            };
        } else {
            double overshoot = spentPct - 100;
            return new String[]{
                String.format("🚨 Over Budget! You have exceeded your salary by %.1f%%. Review your expenses immediately.", overshoot),
                "danger"
            };
        }
    }
}
