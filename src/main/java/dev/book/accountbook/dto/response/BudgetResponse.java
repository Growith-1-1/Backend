package dev.book.accountbook.dto.response;

import dev.book.accountbook.entity.Budget;

public record BudgetResponse(Long id, int budget, long total) {
    public static BudgetResponse from(Budget budget, int total) {
        return new BudgetResponse(
                budget.getId(),
                budget.getBudgetLimit(),
                total
        );
    }
}
