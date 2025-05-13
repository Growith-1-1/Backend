package dev.book.accountbook.repository.querydsl.account;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.entity.QAccountBook;
import dev.book.accountbook.type.CategoryType;
import dev.book.global.entity.QCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class AccountBookRepositoryImpl implements AccountBookRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AccountBook> findByAccountBookWithPage(Long userId, CategoryType categoryType, Pageable pageable) {
        QAccountBook accountBook = QAccountBook.accountBook;
        QCategory category = QCategory.category1;

        List<Long> ids = queryFactory
                .select(accountBook.id)
                .from(accountBook)
                .where(accountBook.user.id.eq(userId),
                        accountBook.type.eq(categoryType))
                .orderBy(accountBook.occurredAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (ids.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<AccountBook> content = queryFactory
                .selectFrom(accountBook)
                .join(accountBook.category, category).fetchJoin()
                .where(accountBook.id.in(ids))
                .orderBy(accountBook.occurredAt.desc())
                .fetch();

        Long totalCount = queryFactory
                .select(accountBook.count())
                .from(accountBook)
                .where(
                        accountBook.user.id.eq(userId),
                        accountBook.type.eq(categoryType)
                ).fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        return new PageImpl<>(content, pageable, total);
    }
}
