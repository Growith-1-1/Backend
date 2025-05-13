package dev.book.accountbook.controller.swagger;

import dev.book.accountbook.dto.request.AccountBookIncomeRequest;
import dev.book.accountbook.dto.request.AccountBookMonthRequest;
import dev.book.accountbook.dto.request.AccountBookSpendListRequest;
import dev.book.accountbook.dto.request.AccountBookSpendRequest;
import dev.book.accountbook.dto.response.*;
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
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "가계부 API", description = "지출 / 수입 등록, 수정, 삭제, 조회")
public interface AccountBookApi {

    @Operation(
            summary = "지출 목록 조회",
            description = "유저 ID로 지출을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "지출 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookListResponse.class),
                    examples = @ExampleObject(
                            name = "지출 목록 예시",
                            value = """
                    {
                      "accountBookSpendResponseList": [
                        {
                          "id": 1,
                          "title": "핫도그",
                          "category": "식비",
                          "amount": 2500,
                          "updatedAt": "2025-04-17T23:59:59",
                          "memo": "밤에 배고파서 먹은 야식",
                          "endDate": null,
                          "occurredAt": "2025-04-17",
                          "repeat": null
                        },
                        {
                          "id": 2,
                          "title": "아이스 아메리카노",
                          "category": "카페 / 간식",
                          "amount": 4500,
                          "updatedAt": "2025-04-17T20:30:00",
                          "memo": "오후 커피",
                          "endDate": null,
                          "occurredAt": "2025-04-17",
                          "repeat": null
                        }
                      ],
                      "totalPage": 1,
                      "totalElement": 2,
                      "number": 1
                    }
                    """
                    )
            )
    )
    ResponseEntity<AccountBookListResponse> getSpendList(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam int page);

    @Operation(summary = "지출 상제 조회", description = "지출 ID를 이용해 지출을 상제 조회합니다.")
    @Parameter(name = "id", description = "지출 id", example = "1")
    @ApiResponse(
            responseCode = "200",
            description = "지출 상세 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookResponse.class),
                    examples = @ExampleObject(
                            name = "지출 상세 예시",
                            value = """
                                    {
                                      "id": 1,
                                      "title": "핫도그",
                                      "category": "식비",
                                      "amount": 2500,
                                      "updatedAt": "2025-04-17T23:59:59",
                                      "memo": "밤에 배고파서 먹은 야식",
                                      "endDate": null,
                                      "occurredAt" : "2025-04-17",
                                      "repeat": null
                                    }
                                    """
                    )
            )
    )
    ResponseEntity<AccountBookResponse> getSpendOne(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id);

    @Operation(summary = "지출 등록", description = "일반/정기 지출을 등록합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "지출 등록 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookResponse.class),
                    examples = @ExampleObject(
                            name = "지출 상세 예시",
                            value = """
                                    {
                                      "id": 1,
                                      "title": "핫도그",
                                      "category": "식비",
                                      "amount": 2500,
                                      "updatedAt": "2025-04-17T23:59:59",
                                      "memo": "밤에 배고파서 먹은 야식",
                                      "endDate": null,
                                      "occurredAt" : "2025-04-17",
                                      "repeat": {
                                        "frequency": "monthly",
                                        "month": null,
                                        "day": 10
                                      }
                                    }
                                    """
                    )
            )
    )
    ResponseEntity<AccountBookResponse> createSpend(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                         @Valid @RequestBody AccountBookSpendRequest request);

    @Operation(summary = "지출 수정", description = "지출 정보를 수정합니다.")
    @Parameter(name = "id", description = "지출 id", example = "1")
    @ApiResponse(
            responseCode = "200",
            description = "지출 수정 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookResponse.class),
                    examples = @ExampleObject(
                            name = "지출 상세 예시",
                            value = """
                                    {
                                      "id": 1,
                                      "title": "핫도그",
                                      "category": "식비",
                                      "amount": 2500,
                                      "updatedAt": "2025-04-17T23:59:59",
                                      "memo": "밤에 배고파서 먹은 야식",
                                      "endDate": null,
                                      "occurredAt" : "2025-04-17",
                                      "repeat": {
                                        "frequency": "monthly",
                                        "month": null,
                                        "day": 10
                                      }
                                    }
                                    """
                    )
            )
    )
    ResponseEntity<AccountBookResponse> modifySpend(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookSpendRequest request, @PathVariable Long id);

    @Operation(summary = "지출 삭제", description = "지출 정보를 삭제합니다.")
    @Parameter(name = "id", description = "지출 id", example = "1")
    ResponseEntity<Boolean> deleteSpend(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id);

    @Operation(summary = "수입 목록 조회", description = "유저 ID를 기반으로 수입 목록을 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "수입 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookListResponse.class),
                    examples = @ExampleObject(
                            name = "수입 목록 예시",
                            value = """
                    {
                      "accountBookResponseList": [
                        {
                          "id": 1,
                          "title": "월급",
                          "category": "급여",
                          "amount": 3000000,
                          "updatedAt": "2025-04-10T23:59:59",
                          "memo": "4월 정기 급여",
                          "endDate": "2025-12-31T23:59:59",
                          "occurredAt": "2025-04-17",
                          "repeat": {
                            "frequency": "monthly",
                            "month": null,
                            "day": 10
                          }
                        },
                        {
                          "id": 2,
                          "title": "이자 수익",
                          "category": "저축 / 투자",
                          "amount": 15000,
                          "updatedAt": "2025-04-10T10:00:00",
                          "memo": "적금 이자",
                          "endDate": null,
                          "occurredAt": "2025-04-17",
                          "repeat": null
                        }
                      ],
                      "totalPage": 1,
                      "totalElement": 2,
                      "number": 1
                    }
                    """
                    )
            )
    )
    ResponseEntity<AccountBookListResponse> getIncomeList(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam int page);

    @Parameter(name = "id", description = "수입 ID", example = "1")
    @Operation(summary = "수입 상세 조회", description = "수입 ID를 이용해 수입 상세 정보를 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "수입 상세 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookResponse.class),
                    examples = @ExampleObject(
                            name = "수입 상세 예시",
                            value = """
                                    {
                                      "id": 1,
                                      "title": "월급",
                                      "category": "급여",
                                      "amount": 3000000,
                                      "updatedAt": "2025-04-10T23:59:59",
                                      "memo": "4월 정기 급여",
                                      "endDate": "2025-12-31T23:59:59",
                                      "occurredAt" : "2025-04-17",
                                      "repeat": {
                                        "frequency": "monthly",
                                        "month": null,
                                        "day": 10
                                      }
                                    }
                                    """
                    )
            )
    )
    ResponseEntity<AccountBookResponse> getIncomeOne(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id);

    @Operation(summary = "수입 등록", description = "새로운 수입 정보를 등록합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "수입 등록 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookResponse.class),
                    examples = @ExampleObject(
                            name = "수입 상세 예시",
                            value = """
                                    {
                                      "id": 1,
                                      "title": "월급",
                                      "category": "급여",
                                      "amount": 3000000,
                                      "updatedAt": "2025-04-10T23:59:59",
                                      "memo": "4월 정기 급여",
                                      "endDate": "2025-12-31T23:59:59",
                                      "occurredAt" : "2025-04-17",
                                      "repeat": {
                                        "frequency": "monthly",
                                        "month": null,
                                        "day": 10
                                      }
                                    }
                                    """
                    )
            )
    )
    ResponseEntity<AccountBookResponse> createIncome(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookIncomeRequest request);

    @Parameter(name = "id", description = "수입 id", example = "1")
    @Operation(summary = "수입 수정", description = "수입 정보를 수정합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "수입 수정 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookResponse.class),
                    examples = @ExampleObject(
                            name = "수입 상세 예시",
                            value = """
                                    {
                                      "id": 1,
                                      "title": "월급",
                                      "category": "급여",
                                      "amount": 3000000,
                                      "updatedAt": "2025-04-10T23:59:59",
                                      "memo": "4월 정기 급여",
                                      "endDate": "2025-12-31T23:59:59",
                                      "occurredAt" : "2025-04-17",
                                      "repeat": {
                                        "frequency": "monthly",
                                        "month": null,
                                        "day": 10
                                      }
                                    }
                                    """
                    )
            )
    )
    ResponseEntity<AccountBookResponse> modifyIncome(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookIncomeRequest request, @PathVariable Long id);

    @Parameter(name = "id", description = "수입 id", example = "1")
    @Operation(summary = "수입 삭제", description = "수입 정보를 삭제합니다.")
    ResponseEntity<Boolean> deleteIncome(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id);

    @Operation(
            summary = "카테고리별 지출 조회",
            description = "카테고리 이름을 이용해 지출 목록을 조회합니다."
    )
    @Parameters({
            @Parameter(
                    name = "category",
                    description = "카테고리 이름",
                    example = "food"
            ),
            @Parameter(
                    name = "page",
                    description = "페이지 번호 (0부터 시작)",
                    example = "0"
            )
    })
    @ApiResponse(
            responseCode = "200",
            description = "카테고리 기준 지출 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookListResponse.class),
                    examples = @ExampleObject(
                            name = "지출 목록 예시",
                            value = """
                    {
                      "accountBookSpendResponseList": [
                        {
                          "id": 1,
                          "title": "핫도그",
                          "category": "식비",
                          "amount": 2500,
                          "updatedAt": "2025-04-17T23:59:59",
                          "memo": "밤에 배고파서 먹은 야식",
                          "endDate": "2025-12-31T23:59:59",
                          "occurredAt": "2025-04-17",
                          "repeat": {
                            "frequency": "yearly",
                            "month": 4,
                            "day": 17
                          }
                        },
                        {
                          "id": 2,
                          "title": "치킨",
                          "category": "식비",
                          "amount": 4500,
                          "updatedAt": "2025-04-17T20:30:00",
                          "memo": "야식은 역시 치킨",
                          "endDate": null,
                          "occurredAt": "2025-04-17",
                          "repeat": null
                        }
                      ],
                      "totalPage": 1,
                      "totalElement": 2,
                      "number": 1
                    }
                    """
                    )
            )
    )
    ResponseEntity<AccountBookListResponse> getCategorySpendList(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam String category, @RequestParam int page);

    @Operation(
            summary = "지출 목록 생성",
            description = "지출 목록을 한 번에 등록합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "지출 목록 등록 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AccountBookResponse.class)),
                    examples = @ExampleObject(
                            name = "지출 생성 응답 예시",
                            value = """
                                    [
                                      {
                                        "id": 1,
                                        "title": "핫도그",
                                        "category": "음식",
                                        "amount": 2500,
                                        "updatedAt": "2025-04-17T23:59:59",
                                        "memo": "밤에 배고파서 먹은 야식",
                                        "endDate": null,
                                        "occurredAt" : "2025-04-17",
                                        "repeat": {
                                          "frequency": "monthly",
                                          "month": null,
                                          "day": 10
                                        }
                                      }
                                    ]
                                    """
                    )
            )
    )
    ResponseEntity<List<AccountBookResponse>> createSpendList(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookSpendListRequest requestList);

    @Operation(
            summary = "지정 기간 가계부 조회",
            description = "사용자가 요청한 기간 동안의 수입 및 지출 내역을 반환합니다."
    )
    @Parameters({
            @Parameter(
                    name = "startDate",
                    description = "조회 시작 날짜 (yyyy-MM-dd 형식)",
                    example = "2025-04-01"
            ),
            @Parameter(
                    name = "endDate",
                    description = "조회 종료 날짜 (yyyy-MM-dd 형식)",
                    example = "2025-04-30"
            ),
            @Parameter(
                    name = "page",
                    description = "조회할 페이지 번호",
                    example = "1"
            )
    })
    @ApiResponse(
            responseCode = "200",
            description = "기간별 가계부 내역 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookPeriodListResponse.class),
                    examples = @ExampleObject(
                            name = "기간별 가계부 응답 예시",
                            value = """
                    {
                      "accountBookPeriodResponse": [
                        {
                          "id": 1,
                          "title": "월급",
                          "type": "INCOME",
                          "category": "급여",
                          "amount": 3000000,
                          "memo": "4월 급여",
                          "endDate": null,
                          "occurredAt": "2025-04-10",
                          "repeat": null
                        },
                        {
                          "id": 2,
                          "title": "편의점",
                          "type": "SPEND",
                          "category": "편의점 / 마트 / 잡화",
                          "amount": 4500,
                          "memo": "컵라면",
                          "endDate": null,
                          "occurredAt": "2025-04-11",
                          "repeat": null
                        }
                      ],
                      "totalPage": 1,
                      "totalElement": 2,
                      "number": 1
                    }
                    """
                    )
            )
    )
    ResponseEntity<AccountBookPeriodListResponse> getAccountBookPeriod(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate,  @RequestParam int page);

    @Operation(
            summary = "지정 월 가계부 조회",
            description = "사용자가 요청한 기간 동안의 수입 및 지출 내역을 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "기간별 가계부 내역 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AccountBookPeriodResponse.class)),
                    examples = @ExampleObject(
                            name = "기간별 가계부 응답 예시",
                            value = """
                                        [
                                           {
                                             "day": 1,
                                             "spendTotal": 15000,
                                             "incomeTotal": 0,
                                             "dayList": [
                                               {
                                                 "id": 101,
                                                 "title": "편의점",
                                                 "type": "SPEND",
                                                 "category": "편의점 / 마트 / 잡화",
                                                 "amount": 15000,
                                                 "memo": "야식",
                                                 "occurredAt": "2025-04-01"
                                               }
                                             ]
                                           },
                                           {
                                             "day": 2,
                                             "spendTotal": 0,
                                             "incomeTotal": 0,
                                             "dayList": []
                                           },
                                           {
                                             "day": 3,
                                             "spendTotal": 0,
                                             "incomeTotal": 500000,
                                             "dayList": [
                                               {
                                                 "id": 102,
                                                 "title": "용돈",
                                                 "type": "INCOME",
                                                 "category": "이체",
                                                 "amount": 500000,
                                                 "memo": "부모님",
                                                 "occurredAt": "2025-04-03"
                                               }
                                             ]
                                           },
                                           {
                                             "day": 4,
                                             "spendTotal": 20000,
                                             "incomeTotal": 0,
                                             "dayList": [
                                               {
                                                 "id": 103,
                                                 "title": "카페",
                                                 "type": "SPEND",
                                                 "category": "카페 / 간식",
                                                 "amount": 20000,
                                                 "memo": "아아+디저트",
                                                 "occurredAt": "2025-04-04"
                                               }
                                             ]
                                           }
                                         ]
                                    """
                    )
            )
    )
    ResponseEntity<List<AccountBookMonthResponse>> getMonthAccountBook(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody AccountBookMonthRequest request);

    @Operation(
            summary = "임시 가계부 목록 조회",
            description = "임시 가계부 목록을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "임시 가계부 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = TempAccountBookResponse.class)),
                    examples = @ExampleObject(
                            name = "임시 가계부 생성 응답 예시",
                            value = """
                                    [
                                      {
                                        "id": 1,
                                        "title": "편의점",
                                        "memo": "야식으로 컵라면",
                                        "amount": 4500,
                                        "type": "SPEND",
                                        "occurredAt": "2025-04-27",
                                        "userId": 5
                                      },
                                      {
                                        "id": 2,
                                        "title": "급여",
                                        "memo": "4월 월급",
                                        "amount": 3000000,
                                        "type": "INCOME",
                                        "occurredAt": "2025-04-25",
                                        "userId": 5
                                      }
                                    ]
                                    """
                    )
            )
    )
    ResponseEntity<List<TempAccountBookResponse>> getTempAccountBook(@AuthenticationPrincipal CustomUserDetails userDetails);
}
