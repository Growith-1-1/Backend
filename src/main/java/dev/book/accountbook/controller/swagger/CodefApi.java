package dev.book.accountbook.controller.swagger;

import dev.book.accountbook.dto.request.CreateConnectedIdRequest;
import dev.book.accountbook.dto.response.TempAccountBookResponse;
import dev.book.global.config.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Codef API", description = "connectedId 생성, 거래 내역 조회")
public interface CodefApi {
    @Operation(
            summary = "[호출 금지] 테스트 용도",
            description = "이 API는 내부 테스트용입니다. 호출하지 마세요."
    )
    @ApiResponse(description = "호출 금지")
    void token();

    @Operation(
            summary = "Codef 연결 계정 생성",
            description = "사용자의 ID/PW를 통해 Connected ID를 생성합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Connected ID 생성 성공"
    )
    ResponseEntity<Boolean> connect(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody CreateConnectedIdRequest request);

    @Operation(
            summary = "[호출 금지] 테스트 용도",
            description = "이 API는 내부 테스트용입니다. 호출하지 마세요."
    )
    @ApiResponse(description = "호출 금지")
    ResponseEntity<List<TempAccountBookResponse>> trans(@AuthenticationPrincipal CustomUserDetails userDetails);
}
