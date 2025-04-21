package dev.book.accountbook.controller.swagger;

import dev.book.accountbook.dto.request.AccountBookIncomeRequest;
import dev.book.accountbook.dto.request.AccountBookSpendListRequest;
import dev.book.accountbook.dto.request.AccountBookSpendRequest;
import dev.book.accountbook.dto.response.AccountBookIncomeResponse;
import dev.book.accountbook.dto.response.AccountBookSpendResponse;
import dev.book.global.config.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.List;

@Tag(name = "가계부 API", description = "지출 / 수입 등록, 수정, 삭제, 조회")
public interface AccountBookApi {

    @Operation(summary = "지출 목록 조회", description = "유저 ID로 지출을 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "지출 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AccountBookSpendResponse.class)),
                    examples = @ExampleObject(
                            name = "지출 목록 예시",
                            value = """
                                    [
                                      {
                                        "id": 1,
                                        "title": "핫도그",
                                        "category": "food",
                                        "amount": 2500,
                                        "updatedAt": "2025-04-17T23:59:59",
                                        "memo": "밤에 배고파서 먹은 야식",
                                        "endDate": null,
                                        "repeat": null
                                      },
                                      {
                                        "id": 2,
                                        "title": "아이스 아메리카노",
                                        "category": "cafe_snack",
                                        "amount": 4500,
                                        "updatedAt": "2025-04-17T20:30:00",
                                        "memo": "오후 커피",
                                        "endDate": null,
                                        "repeat": null
                                      }
                                    ]
                                    
                                    """
                    )
            )
    )
    ResponseEntity<List<AccountBookSpendResponse>> getSpendList(@AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "지출 상제 조회", description = "지출 ID를 이용해 지출을 상제 조회합니다.")
    @Parameter(name = "id", description = "지출 id", example = "1")
    @ApiResponse(
            responseCode = "200",
            description = "지출 상세 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookSpendResponse.class),
                    examples = @ExampleObject(
                            name = "지출 상세 예시",
                            value = """
                                    {
                                      "id": 1,
                                      "title": "핫도그",
                                      "category": "food",
                                      "amount": 2500,
                                      "updatedAt": "2025-04-17T23:59:59",
                                      "memo": "밤에 배고파서 먹은 야식",
                                      "endDate": null,
                                      "repeat": null
                                    }
                                    """
                    )
            )
    )
    ResponseEntity<AccountBookSpendResponse> getSpendOne(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id);

    @Operation(summary = "지출 등록", description = "일반/정기 지출을 등록합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "지출 등록 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookSpendResponse.class),
                    examples = @ExampleObject(
                            name = "지출 상세 예시",
                            value = """
                                    {
                                      "id": 1,
                                      "title": "핫도그",
                                      "category": "food",
                                      "amount": 2500,
                                      "updatedAt": "2025-04-17T23:59:59",
                                      "memo": "밤에 배고파서 먹은 야식",
                                      "endDate": null,
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
    ResponseEntity<AccountBookSpendResponse> createSpend(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                         @Valid @RequestBody AccountBookSpendRequest request);

    @Operation(summary = "지출 수정", description = "지출 정보를 수정합니다.")
    @Parameter(name = "id", description = "지출 id", example = "1")
    @ApiResponse(
            responseCode = "200",
            description = "지출 수정 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookSpendResponse.class),
                    examples = @ExampleObject(
                            name = "지출 상세 예시",
                            value = """
                                    {
                                      "id": 1,
                                      "title": "핫도그",
                                      "category": "food",
                                      "amount": 2500,
                                      "updatedAt": "2025-04-17T23:59:59",
                                      "memo": "밤에 배고파서 먹은 야식",
                                      "endDate": null,
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
    ResponseEntity<AccountBookSpendResponse> modifySpend(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookSpendRequest request, @PathVariable Long id);

    @Operation(summary = "지출 삭제", description = "지출 정보를 삭제합니다.")
    @Parameter(name = "id", description = "지출 id", example = "1")
    ResponseEntity<Boolean> deleteSpend(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id);

    @Operation(summary = "수입 목록 조회", description = "유저 ID를 기반으로 수입 목록을 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "수입 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AccountBookIncomeResponse.class)),
                    examples = @ExampleObject(
                            name = "수입 목록 예시",
                            value = """
                                    [
                                      {
                                        "id": 1,
                                        "title": "월급",
                                        "category": "salary",
                                        "amount": 3000000,
                                        "updatedAt": "2025-04-10T23:59:59",
                                        "memo": "4월 정기 급여",
                                        "endDate": "2025-12-31T23:59:59",
                                        "repeat": {
                                          "frequency": "MONTHLY",
                                          "month": null,
                                          "day": 10
                                        }
                                      },
                                      {
                                        "id": 2,
                                        "title": "이자 수익",
                                        "category": "saving_investment",
                                        "amount": 15000,
                                        "updatedAt": "2025-04-10T10:00:00",
                                        "memo": "적금 이자",
                                        "endDate": null,
                                        "repeat": null
                                      }
                                    ]
                                    """
                    )
            )
    )
    ResponseEntity<List<AccountBookIncomeResponse>> getIncomeList(@AuthenticationPrincipal CustomUserDetails userDetails);

    @Parameter(name = "id", description = "수입 ID", example = "1")
    @Operation(summary = "수입 상세 조회", description = "수입 ID를 이용해 수입 상세 정보를 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "수입 상세 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookIncomeResponse.class),
                    examples = @ExampleObject(
                            name = "수입 상세 예시",
                            value = """
                                    {
                                      "id": 1,
                                      "title": "월급",
                                      "category": "salary",
                                      "amount": 3000000,
                                      "updatedAt": "2025-04-10T23:59:59",
                                      "memo": "4월 정기 급여",
                                      "endDate": "2025-12-31T23:59:59",
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
    ResponseEntity<AccountBookIncomeResponse> getIncomeOne(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id);

    @Operation(summary = "수입 등록", description = "새로운 수입 정보를 등록합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "수입 등록 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookIncomeResponse.class),
                    examples = @ExampleObject(
                            name = "수입 상세 예시",
                            value = """
                                    {
                                      "id": 1,
                                      "title": "월급",
                                      "category": "salary",
                                      "amount": 3000000,
                                      "updatedAt": "2025-04-10T23:59:59",
                                      "memo": "4월 정기 급여",
                                      "endDate": "2025-12-31T23:59:59",
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
    ResponseEntity<AccountBookIncomeResponse> createIncome(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookIncomeRequest request);

    @Parameter(name = "id", description = "수입 id", example = "1")
    @Operation(summary = "수입 수정", description = "수입 정보를 수정합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "수입 수정 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountBookIncomeResponse.class),
                    examples = @ExampleObject(
                            name = "수입 상세 예시",
                            value = """
                                    {
                                      "id": 1,
                                      "title": "월급",
                                      "category": "salary",
                                      "amount": 3000000,
                                      "updatedAt": "2025-04-10T23:59:59",
                                      "memo": "4월 정기 급여",
                                      "endDate": "2025-12-31T23:59:59",
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
    ResponseEntity<AccountBookIncomeResponse> modifyIncome(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookIncomeRequest request, @PathVariable Long id);

    @Parameter(name = "id", description = "수입 id", example = "1")
    @Operation(summary = "수입 삭제", description = "수입 정보를 삭제합니다.")
    ResponseEntity<Boolean> deleteIncome(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id);

    @Parameter(name = "category", description = "카테고리 이름", example = "food")
    @Operation(summary = "카테고리별 지출 조회", description = "카테고리 이름을 이용해 지출 목록을 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "카테고리 기준 지출 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AccountBookSpendResponse.class)),
                    examples = @ExampleObject(
                            name = "지출 목록 예시",
                            value = """
                                    [
                                      {
                                        "id": 1,
                                        "title": "핫도그",
                                        "category": "food",
                                        "amount": 2500,
                                        "updatedAt": "2025-04-17T23:59:59",
                                        "memo": "밤에 배고파서 먹은 야식",
                                        "endDate": "2025-12-31T23:59:59",
                                        "repeat": {
                                          "frequency": "yearly",
                                          "month": 4,
                                          "day": 17
                                        }
                                      },
                                      {
                                        "id": 2,
                                        "title": "치킨",
                                        "category": "food",
                                        "amount": 4500,
                                        "updatedAt": "2025-04-17T20:30:00",
                                        "memo": "야식은 역시 치킨",
                                        "endDate": null,
                                        "repeat": null
                                      }
                                    ]
                                    """
                    )
            )
    )
    ResponseEntity<List<AccountBookSpendResponse>> getCategorySpendList(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable String category);

    @Operation(
            summary = "지출 목록 생성",
            description = "지출 목록을 한 번에 등록합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "지출 목록 등록 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AccountBookSpendResponse.class)),
                    examples = @ExampleObject(
                            name = "지출 생성 응답 예시",
                            value = """
                                [
                                  {
                                    "id": 1,
                                    "title": "핫도그",
                                    "category": "food",
                                    "amount": 2500,
                                    "updatedAt": "2025-04-17T23:59:59",
                                    "memo": "밤에 배고파서 먹은 야식",
                                    "endDate": null,
                                    "repeat": null
                                  }
                                ]
                                """
                    )
            )
    )
    ResponseEntity<List<AccountBookSpendResponse>> createSpendList(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @RequestBody AccountBookSpendListRequest requestList);
}
