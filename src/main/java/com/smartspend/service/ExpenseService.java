package com.smartspend.service;

import com.smartspend.dto.expense.ExpenseRequest;
import com.smartspend.dto.expense.ExpenseResponse;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.model.entity.Expense;
import com.smartspend.model.entity.User;
import com.smartspend.model.enums.Category;
import com.smartspend.repository.ExpenseRepository;
import com.smartspend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for expense CRUD operations.
 * All methods are scoped to the authenticated user.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository    userRepository;

    // ── Helpers ───────────────────────────────────────────────────────────

    /**
     * Get the currently authenticated user from the security context.
     */
    private User currentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ── Read ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses() {
        Long userId = currentUser().getId();
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(userId)
                .stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByMonth(int year, int month) {
        Long userId = currentUser().getId();
        return expenseRepository.findByUserIdAndMonth(userId, year, month)
                .stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByCategory(Category category) {
        Long userId = currentUser().getId();
        return expenseRepository.findByUserIdAndCategoryOrderByExpenseDateDesc(userId, category)
                .stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getById(Long id) {
        Long userId = currentUser().getId();
        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        return ExpenseResponse.from(expense);
    }

    // ── Create ────────────────────────────────────────────────────────────

    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {
        User user = currentUser();

        Expense expense = Expense.builder()
                .title(request.getTitle())
                .amount(request.getAmount())
                .category(request.getCategory())
                .expenseDate(request.getExpenseDate())
                .notes(request.getNotes())
                .user(user)
                .build();

        expense = expenseRepository.save(expense);
        log.info("Expense created: '{}' by user {}", expense.getTitle(), user.getEmail());
        return ExpenseResponse.from(expense);
    }

    // ── Update ────────────────────────────────────────────────────────────

    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Long userId = currentUser().getId();

        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        expense.setTitle(request.getTitle());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setNotes(request.getNotes());

        expense = expenseRepository.save(expense);
        log.info("Expense updated: id={}", id);
        return ExpenseResponse.from(expense);
    }

    // ── Delete ────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        Long userId = currentUser().getId();

        Expense expense = expenseRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        expenseRepository.delete(expense);
        log.info("Expense deleted: id={}", id);
    }
}
