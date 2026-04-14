package com.smartspend.service;

import com.smartspend.dto.budget.BudgetRequest;
import com.smartspend.dto.budget.BudgetResponse;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.model.entity.Budget;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manages per-category budget limits and computes spending progress.
 */
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository  budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository    userRepository;

    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ── Read ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<BudgetResponse> getAllBudgets() {
        User user = currentUser();
        int year  = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        // Build a quick spend-by-category map for current month
        Map<Category, BigDecimal> spendMap = expenseRepository
            .getSpendingByCategory(user.getId(), year, month)
            .stream()
            .collect(Collectors.toMap(
                r -> (Category) r[0],
                r -> (BigDecimal) r[1]
            ));

        return budgetRepository.findByUserId(user.getId())
                .stream()
                .map(b -> buildResponse(b, spendMap.getOrDefault(b.getCategory(), BigDecimal.ZERO)))
                .toList();
    }

    // ── Upsert (Create or Update) ─────────────────────────────────────────

    @Transactional
    public BudgetResponse setBudget(BudgetRequest request) {
        User user = currentUser();

        // Find existing budget for this category or create new
        Budget budget = budgetRepository.findByUserIdAndCategory(user.getId(), request.getCategory())
                .orElse(Budget.builder()
                        .user(user)
                        .category(request.getCategory())
                        .build());

        budget.setLimitAmount(request.getLimitAmount());
        budget = budgetRepository.save(budget);

        int year  = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();

        BigDecimal spent = expenseRepository
            .getSpendingByCategory(user.getId(), year, month)
            .stream()
            .filter(r -> r[0].equals(request.getCategory()))
            .map(r -> (BigDecimal) r[1])
            .findFirst()
            .orElse(BigDecimal.ZERO);

        return buildResponse(budget, spent);
    }

    // ── Delete ────────────────────────────────────────────────────────────

    @Transactional
    public void deleteBudget(Long id) {
        User user = currentUser();
        Budget budget = budgetRepository.findById(id)
                .filter(b -> b.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + id));
        budgetRepository.delete(budget);
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private BudgetResponse buildResponse(Budget budget, BigDecimal spent) {
        BigDecimal limit     = budget.getLimitAmount();
        BigDecimal remaining = limit.subtract(spent);
        double percent       = spent.divide(limit, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .doubleValue();

        return BudgetResponse.builder()
                .id(budget.getId())
                .category(budget.getCategory())
                .categoryDisplayName(budget.getCategory().getDisplayName())
                .categoryEmoji(budget.getCategory().getEmoji())
                .limitAmount(limit)
                .amountSpent(spent)
                .remaining(remaining)
                .progressPercent(Math.min(percent, 100.0))
                .overLimit(spent.compareTo(limit) > 0)
                .build();
    }
}
