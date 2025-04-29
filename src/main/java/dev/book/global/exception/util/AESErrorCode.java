package dev.book.global.exception.util;

import dev.book.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AESErrorCode implements ErrorCode {
    FAIL_ENCRYPT(HttpStatus.INTERNAL_SERVER_ERROR ,"암호화에 실패했습니다."),
    FAIL_DECRYPT(HttpStatus.INTERNAL_SERVER_ERROR ,"복호화에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
