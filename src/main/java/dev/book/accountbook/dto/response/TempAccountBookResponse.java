package dev.book.accountbook.dto.response;

import dev.book.accountbook.entity.TempAccountBook;
import dev.book.accountbook.type.CategoryType;

import java.time.LocalDate;

public record TempAccountBookResponse(Long id, String title, String memo, int amount,
                                      CategoryType type, LocalDate occurredAt, Long userId) {
    public static TempAccountBookResponse from(TempAccountBook entity) {
        return new TempAccountBookResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getMemo(),
                entity.getAmount(),
                entity.getType(),
                entity.getOccurredAt(),
                entity.getUser().getId()
        );
    }
}
