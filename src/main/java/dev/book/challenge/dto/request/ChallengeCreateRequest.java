package dev.book.challenge.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ChallengeCreateRequest(@Schema(defaultValue = "제목") @NotNull String title,
                                     @Schema(defaultValue = "내용") @NotNull String text,
                                     @Schema(defaultValue = "PUBLIC") @NotNull String release,
                                     @Schema(defaultValue = "10000") @Min(value = 1000, message = "1000원 이상 입력 가능합니다.") Integer amount,
                                     @Schema(defaultValue = "1") @Min(value = 1, message = "1명 부터 입력 가능합니다.") Integer capacity,
                                     @Schema(defaultValue = "[\"FOOD\"]") @NotNull List<String> categoryList,
                                     @Schema(defaultValue = "2024-04-18") @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                     @Schema(defaultValue = "2024-04-19") @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

}
