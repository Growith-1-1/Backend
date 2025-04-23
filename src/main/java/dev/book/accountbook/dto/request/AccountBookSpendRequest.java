package dev.book.accountbook.dto.request;

import dev.book.accountbook.type.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AccountBookSpendRequest(
        @NotBlank
        @Size(max = 50)
        @Schema(description = "지출 이름", example = "핫도그")
        String title,

        @NotNull
        @Min(10)
        @Max(100000000)
        @Schema(description = "지출 금액", example = "2500")
        int amount,

        @Size(max = 1000)
        @Schema(description = "메모", example = "밤에 배고파서 먹은 야식")
        String memo,

        @Schema(description = "정기 지출 종료일", example = "2025-12-31 23:59:59")
        LocalDateTime endDate,

        @NotNull
        @Schema(description = "실제 지출 발생한 시간 수동 입력", example = "2025-04-17T23:00:00")
        LocalDate occurredAt,

        Repeat repeat,
        @Schema(description = "카테고리", example = "food", allowableValues = {
                "food", "cafe_snack", "convenience_store", "alcohol_entertainment", "shopping",
                "hobby", "health", "housing_communication", "finance", "beauty",
                "transportation", "travel", "education", "living", "donation",
                "card_payment", "deferred_payment", "none"
        })
        String category
) implements AccountBookRequest {
    @Override
    public CategoryType categoryType() {
        return CategoryType.SPEND;
    }
}