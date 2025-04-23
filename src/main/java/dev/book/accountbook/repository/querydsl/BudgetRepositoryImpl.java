package dev.book.accountbook.repository.querydsl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.book.accountbook.dto.response.BudgetResponse;
import dev.book.accountbook.entity.QAccountBook;
import dev.book.accountbook.entity.QBudget;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BudgetRepositoryImpl implements BudgetRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public BudgetResponse findBudgetWithTotal(Long userId) {
        QBudget budget = QBudget.budget;
        QAccountBook accountBook = QAccountBook.accountBook;

        return queryFactory
                .select(Projections.constructor(BudgetResponse.class,
                        budget.budgetLimit,
                        Expressions.numberTemplate(Long.class, "coalesce(sum({0}), 0)", accountBook.amount)
                ))
                .from(budget)
                .leftJoin(accountBook)
                .on(accountBook.user.id.eq(budget.user.id)
                        .and(Expressions.numberTemplate(Integer.class, "month({0})", accountBook.occurredAt)
                                .eq(budget.month))
                )
                .where(budget.user.id.eq(userId))
                .groupBy(budget.budgetLimit)
                .fetchOne();
    }
}
