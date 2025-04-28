package dev.book.accountbook.dto.response;

import dev.book.accountbook.dto.request.Repeat;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.type.CategoryType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AccountBookPeriodResponse(Long id, String title, CategoryType type, String category, int amount,
                                        String memo, LocalDateTime endDate, LocalDate occurredAt, Repeat repeat) {
    public static AccountBookPeriodResponse from(AccountBook entity) {
        return new AccountBookPeriodResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getType(),
                entity.getCategory().getKorean(),
                entity.getAmount(),
                entity.getMemo(),
                entity.getEndDate(),
                entity.getOccurredAt(),
                new Repeat(entity.getFrequency(), entity.getMonth(), entity.getDay())
        );
    }
}
