package dev.book.accountbook.dto.response;

import dev.book.accountbook.dto.request.Repeat;
import dev.book.accountbook.entity.AccountBook;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AccountBookIncomeResponse(
        Long id, String title, String category, int amount, LocalDateTime updatedAt,
        String memo, LocalDateTime endDate, LocalDate occurredAt, Repeat repeat
) {
    public static AccountBookIncomeResponse from(AccountBook entity) {
        return new AccountBookIncomeResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getCategory().getKorean(),
                entity.getAmount(),
                entity.getUpdatedAt(),
                entity.getMemo(),
                entity.getEndDate(),
                entity.getOccurredAt(),
                new Repeat(entity.getFrequency(), entity.getMonth(), entity.getDay())
        );
    }
}