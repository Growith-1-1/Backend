package dev.book.global.config.Firebase.controller.swagger;

import dev.book.global.config.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "FCM API", description = "토큰 저장. 테스트용 토큰 조회 및 메시지 조회")
public interface FcmControllerApi {
    @Operation(
            summary = "FCM 토큰 저장",
            description = "로그인한 사용자의 FCM 토큰을 저장합니다."
    )
    @Parameters({
            @Parameter(name = "token", description = "저장할 FCM 토큰", example = "abcdefg1234567")
    })
    @ApiResponse(
            responseCode = "200",
            description = "토큰 저장 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Boolean.class),
                    examples = @ExampleObject(
                            name = "토큰 저장 성공 예시",
                            value = "true"
                    )
            )
    )
    ResponseEntity<Boolean> saveToken(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam String token);

    @Operation(
            summary = "FCM 토큰 조회",
            description = "로그인한 사용자의 FCM 토큰을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "[테스트용] 저장한 FCM 토큰을 조회할 수 있습니다.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = String.class),
                    examples = @ExampleObject(
                            name = "FCM 토큰 예시",
                            value = "\"abcdefg1234567\""
                    )
            )
    )
    ResponseEntity<String> sendFcmToken(@AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(
            summary = "FCM 메시지 전송",
            description = "[테스트용] 전달될 메시지를 확인해 볼 수 있습니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "메시지 전송 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = String.class),
                    examples = @ExampleObject(
                            name = "FCM 메시지 예시",
                            value = "\"홍길동님, 현재까지 지출은 150000원입니다. 정하신 예산 200000원 에서 75% 만큼 사용하셨습니다.\""
                    )
            )
    )
    ResponseEntity<String> sendFcmMessage(@AuthenticationPrincipal CustomUserDetails userDetails);
}
