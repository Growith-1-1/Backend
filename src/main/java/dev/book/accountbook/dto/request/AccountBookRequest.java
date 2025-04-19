package dev.book.accountbook.dto.request;

import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.type.Category;
import dev.book.accountbook.type.CategoryType;
import dev.book.accountbook.type.Frequency;
import dev.book.user.entity.UserEntity;

import java.time.LocalDateTime;

public interface AccountBookRequest {
    String title();
    Category category();
    int amount();
    String memo();
    LocalDateTime endDate();
    Repeat repeat();
    CategoryType categoryType();

    default AccountBook toEntity(UserEntity user) {
        Frequency frequency = null;
        Integer month = null;
        Integer day = null;

        if (repeat() != null) {
            frequency = repeat().frequency();
            month = repeat().month();
            day = repeat().day();
        }

        return new AccountBook(title(), category(), categoryType(), amount(), endDate(), memo(), user, frequency, month, day);
    }
}
