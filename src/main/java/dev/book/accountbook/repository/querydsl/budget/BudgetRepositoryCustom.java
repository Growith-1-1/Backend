package dev.book.accountbook.repository.querydsl.budget;

import dev.book.accountbook.dto.response.BudgetResponse;

public interface BudgetRepositoryCustom {
    BudgetResponse findBudgetWithTotal(Long userId);
    BudgetResponse findBudgetByUserIdWithTotal(Long userId);
}
