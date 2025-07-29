package dev.book.global.config.firebase.exception;

import dev.book.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FcmTokenErrorCode implements ErrorCode {
    NOT_FOUND_FCM_TOKEN(HttpStatus.NOT_FOUND, "토큰을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;
}
