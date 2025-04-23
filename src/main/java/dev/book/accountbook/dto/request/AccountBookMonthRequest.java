package dev.book.accountbook.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record AccountBookMonthRequest(
        @Schema(description = "캘린더 월, 일은 무조건 1일로 해주세요.", example = "2025-04-01")
        LocalDate requestMonth) {
}
