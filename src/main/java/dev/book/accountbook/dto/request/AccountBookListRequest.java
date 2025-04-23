package dev.book.accountbook.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record AccountBookListRequest(
        @Schema(description = "시작 날", example = "2025-04-01")
        LocalDate startDate,
        @Schema(description = "마지막 날", example = "2025-04-20")
        LocalDate endDate) {
}
