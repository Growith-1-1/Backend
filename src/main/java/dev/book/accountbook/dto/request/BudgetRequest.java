package dev.book.accountbook.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

public record BudgetRequest(
        @Min(10)
        @Schema(description = "예산 금액", example = "1000000", minimum = "10")
        int budget
) {}
