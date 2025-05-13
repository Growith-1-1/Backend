package dev.book.accountbook.repository.querydsl.account;

import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.type.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountBookRepositoryCustom {
    Page<AccountBook> findByAccountBookWithPage(Long userId, CategoryType categoryType, Pageable pageable);
}
