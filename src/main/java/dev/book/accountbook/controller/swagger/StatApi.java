package dev.book.accountbook.controller.swagger;

import dev.book.accountbook.dto.response.AccountBookConsumeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendListResponse;
import dev.book.accountbook.dto.response.AccountBookStatResponse;
import dev.book.accountbook.type.Frequency;
import dev.book.global.config.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "통계 API", description = "소비 많은 카테고리 3개 조회, 카테고리 별 소비 항목 조회, 직전 일간 / 주간 / 월간 소비 비교")
public interface StatApi {

    @Operation(
            summary = "지출 통계 조회",
            description = "일간, 주간, 월간 중 frequency 값을 기반으로 지출 통계 데이터를 조회합니다."
    )
    @Parameter(name = "frequency", description = "통계 조회 단위 (daily, weekly, monthly)",
            schema = @Schema(type = "string", allowableValues = {"daily", "weekly", "monthly"}))
    @ApiResponse(
            responseCode = "200",
            description = "지출 통계 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AccountBookStatResponse.class)),
                    examples = @ExampleObject(
                            name = "지출 통계 응답 예시",
                            value = """
            [
              {
                "category": "식비",
                "totalAmount": 15000
              },
              {
                "category": "카페 / 간식",
                "totalAmount": 8700
              },
              {
                "category": "취미 / 여가",
                "totalAmount": 5000
              }
            ]
            """
                    )
            )
    )
    ResponseEntity<List<AccountBookStatResponse>> statList(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Frequency frequency);

    @Operation(
            summary = "카테고리별 지출 목록 조회",
            description = "주어진 기간(frequency)과 카테고리(category)를 기반으로 지출 목록을 조회합니다."
    )
    @Parameters({
            @Parameter(
                    name = "frequency",
                    description = "조회 기준 기간 (daily, weekly, monthly)",
                    example = "monthly",
                    schema = @Schema(type = "string", allowableValues = {"daily", "weekly", "monthly"})
            ),
            @Parameter(
                    name = "category",
                    description = "조회할 카테고리 이름",
                    example = "food"
            ),
            @Parameter(
                    name = "page",
                    description = "페이지 번호",
                    example = "1"
            )
    })
    @ApiResponse(
            responseCode = "200",
            description = "카테고리별 지출 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookSpendListResponse.class),
                    examples = @ExampleObject(
                            name = "카테고리 지출 목록 예시",
                            value = """
                    {
                      "accountBookSpendResponseList": [
                        {
                          "id": 1,
                          "title": "라면",
                          "category": "식비",
                          "amount": 3500,
                          "updatedAt": "2025-04-15T20:30:00",
                          "memo": "야식",
                          "endDate": null,
                          "occurredAt": "2025-04-17",
                          "repeat": null
                        },
                        {
                          "id": 2,
                          "title": "김밥",
                          "category": "식비",
                          "amount": 4500,
                          "updatedAt": "2025-04-16T12:00:00",
                          "memo": "점심",
                          "endDate": null,
                          "occurredAt": "2025-04-17",
                          "repeat": {
                            "frequency": "monthly",
                            "month": null,
                            "day": 15
                          }
                        }
                      ],
                      "totalPage": 1,
                      "totalElement": 2,
                      "number": 0
                    }
                    """
                    )
            )
    )
    ResponseEntity<AccountBookSpendListResponse> categoryList(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Frequency frequency, @RequestParam String category, @RequestParam int page);

    @Operation(
            summary = "소비 증감량 조회",
            description = """
        주어진 기간(frequency) 직전 기간 대비 얼마나 더 소비했는지를 반환합니다.

        예) 4월 소비 - 3월 소비, 이번주 소비 - 저번주 소비, 오늘 소비 - 어제 소비
        """
    )
    @Parameters({
            @Parameter(name = "frequency", description = "비교 기준 기간 (daily, weekly, monthly)", example = "monthly",
                    schema = @Schema(type = "string", allowableValues = {"daily", "weekly", "monthly"}))
    })
    @ApiResponse(
            responseCode = "200",
            description = "소비 증감량 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookConsumeResponse.class),
                    examples = @ExampleObject(
                            name = "소비 증감 예시",
                            value = """
            {
              "consume": 32000
            }
            """
                    )
            )
    )
    ResponseEntity<AccountBookConsumeResponse> consume(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Frequency frequency);
}
