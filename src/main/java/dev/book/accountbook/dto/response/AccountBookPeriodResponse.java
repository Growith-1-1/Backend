package dev.book.accountbook.dto.response;

import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.type.CategoryType;

import java.time.LocalDate;

public record AccountBookPeriodResponse(Long id, String title, CategoryType type, String category, int amount, String memo, LocalDate occurredAt) {
    public static AccountBookPeriodResponse from(AccountBook entity) {
        return new AccountBookPeriodResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getType(),
                entity.getCategory().getKorean(),
                entity.getAmount(),
                entity.getMemo(),
                entity.getOccurredAt());
    }
}
