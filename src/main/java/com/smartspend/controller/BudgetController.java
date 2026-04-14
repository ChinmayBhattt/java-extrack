package com.smartspend.controller;

import com.smartspend.dto.budget.BudgetRequest;
import com.smartspend.dto.budget.BudgetResponse;
import com.smartspend.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for budget management.
 */
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    /** GET /api/budgets – list all budgets with current spending status. */
    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAll() {
        return ResponseEntity.ok(budgetService.getAllBudgets());
    }

    /** POST /api/budgets – create or update a budget for a category (upsert). */
    @PostMapping
    public ResponseEntity<BudgetResponse> setBudget(@Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.setBudget(request));
    }

    /** DELETE /api/budgets/{id} – remove a budget. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}
