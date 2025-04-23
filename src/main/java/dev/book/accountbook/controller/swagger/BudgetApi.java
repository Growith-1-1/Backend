package dev.book.accountbook.controller.swagger;

import dev.book.accountbook.dto.request.BudgetRequest;
import dev.book.accountbook.dto.response.BudgetResponse;
import dev.book.global.config.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Budget", description = "예산 등록 / 조회")
public interface BudgetApi {

    @Operation(
            summary = "예산 및 소비 총액 조회",
            description = "유저 ID에 해당하는 예산과 해당 월의 총 소비 금액을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "예산 및 소비 총액 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BudgetResponse.class),
                    examples = @ExampleObject(
                            name = "예산 응답 예시",
                            value = """
                                    {
                                      "budget": 1000000,
                                      "total": 50000
                                    }
                                    """
                    )
            )
    )
    ResponseEntity<BudgetResponse> getBudget(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id);

    @Operation(
            summary = "예산 등록",
            description = "예산을 등록합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "예산 등록 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BudgetResponse.class),
                    examples = @ExampleObject(
                            name = "예산 응답 예시",
                            value = """
                                    {
                                      "budget": 1000000,
                                      "total": 50000
                                    }
                                    """
                    )
            )
    )
    ResponseEntity<BudgetResponse> createBudget(@AuthenticationPrincipal CustomUserDetails userDetails, BudgetRequest budgetRequest);

    @Operation(
            summary = "예산 수정",
            description = "예산을 수정합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "예산 수정 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = BudgetResponse.class),
                    examples = @ExampleObject(
                            name = "예산 응답 예시",
                            value = """
                                    {
                                      "budget": 1000000,
                                      "total": 50000
                                    }
                                    """
                    )
            )
    )
    ResponseEntity<BudgetResponse> modifyBudget(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, BudgetRequest budgetRequest);

    @Operation(
            summary = "예산 삭제",
            description = "예산을 삭제합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "예산 삭제 성공"
    )
    ResponseEntity<Void> deleteBudget(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id);
}
