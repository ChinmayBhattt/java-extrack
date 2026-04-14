package com.smartspend.controller;

import com.smartspend.dto.expense.ExpenseRequest;
import com.smartspend.dto.expense.ExpenseResponse;
import com.smartspend.model.enums.Category;
import com.smartspend.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for expense CRUD operations.
 * All endpoints are protected — user is identified from JWT.
 */
@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    /**
     * GET /api/expenses
     * Optional query params: ?year=2026&month=4   or   ?category=FOOD
     */
    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Category category
    ) {
        List<ExpenseResponse> result;

        if (year != null && month != null) {
            result = expenseService.getExpensesByMonth(year, month);
        } else if (category != null) {
            result = expenseService.getExpensesByCategory(category);
        } else {
            result = expenseService.getAllExpenses();
        }

        return ResponseEntity.ok(result);
    }

    /** GET /api/expenses/{id} – get a single expense by ID. */
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getById(id));
    }

    /** POST /api/expenses – create a new expense. */
    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.create(request));
    }

    /** PUT /api/expenses/{id} – update an existing expense. */
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody ExpenseRequest request
    ) {
        return ResponseEntity.ok(expenseService.update(id, request));
    }

    /** DELETE /api/expenses/{id} – delete an expense. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
