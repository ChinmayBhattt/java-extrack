package com.smartspend.repository;

import com.smartspend.model.entity.Expense;
import com.smartspend.model.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Data access layer for Expense entities.
 * Contains custom JPQL queries for dashboard analytics.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // ── Basic Filters ─────────────────────────────────────────────────────

    List<Expense> findByUserIdOrderByExpenseDateDesc(Long userId);

    List<Expense> findByUserIdAndCategoryOrderByExpenseDateDesc(Long userId, Category category);

    Optional<Expense> findByIdAndUserId(Long id, Long userId);

    // ── Monthly Filters ───────────────────────────────────────────────────

    @Query("""
        SELECT e FROM Expense e
        WHERE e.user.id = :userId
          AND YEAR(e.expenseDate) = :year
          AND MONTH(e.expenseDate) = :month
        ORDER BY e.expenseDate DESC
        """)
    List<Expense> findByUserIdAndMonth(
        @Param("userId") Long userId,
        @Param("year")   int year,
        @Param("month")  int month
    );

    // ── Aggregations ──────────────────────────────────────────────────────

    @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.user.id = :userId
          AND YEAR(e.expenseDate) = :year
          AND MONTH(e.expenseDate) = :month
        """)
    BigDecimal sumByUserAndMonth(
        @Param("userId") Long userId,
        @Param("year")   int year,
        @Param("month")  int month
    );

    @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.user.id = :userId
        """)
    BigDecimal sumAllByUser(@Param("userId") Long userId);

    // ── Category Breakdown ────────────────────────────────────────────────

    @Query("""
        SELECT e.category, COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.user.id = :userId
          AND YEAR(e.expenseDate) = :year
          AND MONTH(e.expenseDate) = :month
        GROUP BY e.category
        """)
    List<Object[]> getCategoryBreakdown(
        @Param("userId") Long userId,
        @Param("year")   int year,
        @Param("month")  int month
    );

    // ── Monthly Trend (last N months) ─────────────────────────────────────

    @Query("""
        SELECT YEAR(e.expenseDate), MONTH(e.expenseDate), COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.user.id = :userId
        GROUP BY YEAR(e.expenseDate), MONTH(e.expenseDate)
        ORDER BY YEAR(e.expenseDate) DESC, MONTH(e.expenseDate) DESC
        """)
    List<Object[]> getMonthlyTrend(@Param("userId") Long userId);

    // ── Budget Warning: sum by category for current month ────────────────

    @Query("""
        SELECT e.category, COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.user.id = :userId
          AND YEAR(e.expenseDate) = :year
          AND MONTH(e.expenseDate) = :month
        GROUP BY e.category
        """)
    List<Object[]> getSpendingByCategory(
        @Param("userId") Long userId,
        @Param("year")   int year,
        @Param("month")  int month
    );
}
