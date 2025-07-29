package dev.book.accountbook.repository.querydsl.budget;

import dev.book.accountbook.dto.response.BudgetResponse;

import java.time.LocalDate;

public interface BudgetRepositoryCustom {
    BudgetResponse findBudgetWithTotal(Long userId, LocalDate localDate);
    BudgetResponse findBudgetByUserIdWithTotal(Long userId);
}
