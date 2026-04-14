package com.smartspend.repository;

import com.smartspend.model.entity.Budget;
import com.smartspend.model.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for Budget entities.
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    Optional<Budget> findByUserIdAndCategory(Long userId, Category category);

    boolean existsByUserIdAndCategory(Long userId, Category category);
}
