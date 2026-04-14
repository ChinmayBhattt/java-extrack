package com.smartspend.controller;

import com.smartspend.dto.dashboard.DashboardResponse;
import com.smartspend.dto.expense.ExpenseRequest;
import com.smartspend.dto.expense.ExpenseResponse;
import com.smartspend.dto.budget.BudgetResponse;
import com.smartspend.model.enums.Category;
import com.smartspend.service.BudgetService;
import com.smartspend.service.DashboardService;
import com.smartspend.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * Serves Thymeleaf HTML pages (NOT a REST controller).
 *
 * <p>Each method fetches data from the business services and passes it to the template model,
 * following the traditional MVC pattern.</p>
 */
@Controller
@RequiredArgsConstructor
public class PageController {

    private final DashboardService dashboardService;
    private final ExpenseService   expenseService;
    private final BudgetService    budgetService;

    // ── Root redirect ─────────────────────────────────────────────────────

    @GetMapping("/")
    public String root(Authentication auth) {
        return (auth != null && auth.isAuthenticated()) ? "redirect:/dashboard" : "redirect:/login";
    }

    // ── Auth pages (no auth required – served as-is) ──────────────────────

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    // ── Dashboard ─────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardResponse data = dashboardService.getDashboard();
        model.addAttribute("dashboard", data);
        // JSON-serialize breakdown for Chart.js
        model.addAttribute("categoryKeys",   data.getCategoryBreakdown().keySet());
        model.addAttribute("categoryValues", data.getCategoryBreakdown().values());
        model.addAttribute("categoryNames",  data.getCategoryLabels().values());
        return "dashboard/index";
    }

    // ── Expenses ──────────────────────────────────────────────────────────

    @GetMapping("/expenses")
    public String expensesList(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month,
        @RequestParam(required = false) Category category,
        Model model
    ) {
        int resolvedYear  = (year  != null) ? year  : LocalDate.now().getYear();
        int resolvedMonth = (month != null) ? month : LocalDate.now().getMonthValue();

        List<ExpenseResponse> expenses;
        if (category != null) {
            expenses = expenseService.getExpensesByCategory(category);
        } else {
            expenses = expenseService.getExpensesByMonth(resolvedYear, resolvedMonth);
        }

        model.addAttribute("expenses",    expenses);
        model.addAttribute("categories",  Category.values());
        model.addAttribute("currentYear", resolvedYear);
        model.addAttribute("currentMonth", resolvedMonth);
        return "expenses/list";
    }

    @GetMapping("/expenses/new")
    public String newExpenseForm(Model model) {
        model.addAttribute("expenseRequest", new ExpenseRequest());
        model.addAttribute("categories",     Category.values());
        model.addAttribute("today",          LocalDate.now().toString());
        return "expenses/form";
    }

    @GetMapping("/expenses/{id}/edit")
    public String editExpenseForm(@PathVariable Long id, Model model) {
        ExpenseResponse expense = expenseService.getById(id);
        model.addAttribute("expense",    expense);
        model.addAttribute("categories", Category.values());
        return "expenses/form";
    }

    // ── Budgets ───────────────────────────────────────────────────────────

    @GetMapping("/budgets")
    public String budgets(Model model) {
        List<BudgetResponse> budgets = budgetService.getAllBudgets();
        model.addAttribute("budgets",    budgets);
        model.addAttribute("categories", Category.values());
        return "budget/index";
    }

    // ── Profile ───────────────────────────────────────────────────────────

    @GetMapping("/profile")
    public String profile() {
        return "user/profile";
    }
}
