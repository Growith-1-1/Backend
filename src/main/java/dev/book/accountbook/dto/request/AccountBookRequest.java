package dev.book.accountbook.dto.request;

import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.type.CategoryType;
import dev.book.accountbook.type.Frequency;
import dev.book.global.entity.Category;
import dev.book.user.entity.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface AccountBookRequest {
    String title();
    int amount();
    String memo();
    LocalDateTime endDate();
    Repeat repeat();
    CategoryType categoryType();
    String category();
    LocalDate occurredAt();

    default AccountBook toEntity(UserEntity user, Category category) {
        Frequency frequency = null;
        Integer month = null;
        Integer day = null;

        if (repeat() != null) {
            frequency = repeat().frequency();
            month = repeat().month();
            day = repeat().day();
        }

        return new AccountBook(title(), categoryType(), amount(), endDate(), memo(), user, frequency, month, day, category, occurredAt());
    }
}
