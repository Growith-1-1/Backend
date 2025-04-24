package dev.book.accountbook.type;

import lombok.Getter;

@Getter
public enum BudgetLimit {
    BUDGET_LIMIT_100(1.0, 3),
    BUDGET_LIMIT_70(0.7, 2),
    BUDGET_LIMIT_50(0.5, 1);

    private final double threshold;
    private final int limitCount;

    BudgetLimit(double threshold, int limitCount) {
        this.threshold = threshold;
        this.limitCount = limitCount;
    }

    public static BudgetLimit limitBudget(double ratio) {
        for (BudgetLimit limit : BudgetLimit.values()) {
            if (ratio >= limit.getThreshold()) {

                return limit;
            }
        }

        return null;
    }
}
