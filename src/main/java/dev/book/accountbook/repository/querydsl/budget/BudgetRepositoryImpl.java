package dev.book.accountbook.repository.querydsl.budget;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.book.accountbook.dto.response.BudgetResponse;
import dev.book.accountbook.entity.QAccountBook;
import dev.book.accountbook.entity.QBudget;
import dev.book.accountbook.type.CategoryType;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@RequiredArgsConstructor
public class BudgetRepositoryImpl implements BudgetRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public BudgetResponse findBudgetWithTotal(Long id, LocalDate localDate) {
        QBudget budget = QBudget.budget;
        QAccountBook accountBook = QAccountBook.accountBook;

        LocalDate startOfMonth = localDate.withDayOfMonth(1);
        LocalDate endOfMonth = localDate.withDayOfMonth(localDate.lengthOfMonth());

        return queryFactory
                .select(Projections.constructor(BudgetResponse.class,
                        budget.id,
                        budget.budgetLimit,
                        Expressions.numberTemplate(Long.class, "coalesce(sum({0}), 0)", accountBook.amount)
                ))
                .from(budget)
                .leftJoin(accountBook)
                .on(accountBook.user.id.eq(budget.user.id)
                        .and(accountBook.type.eq(CategoryType.SPEND))
                        .and(accountBook.occurredAt.between(startOfMonth, endOfMonth))
                )
                .where(budget.id.eq(id))
                .groupBy(budget.id, budget.budgetLimit)
                .fetchOne();
    }

    @Override
    public BudgetResponse findBudgetByUserIdWithTotal(Long userId) {
        QBudget budget = QBudget.budget;
        QAccountBook accountBook = QAccountBook.accountBook;

        return queryFactory
                .select(Projections.constructor(BudgetResponse.class,
                        budget.id,
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
                .groupBy(budget.id, budget.budgetLimit)
                .fetchOne();
    }
}
