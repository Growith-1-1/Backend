package dev.book.accountbook.exception.codef;

import dev.book.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CodefErrorCode implements ErrorCode {
    INVALID_LOGIN_INFO(HttpStatus.BAD_REQUEST, "로그인 정보가 옳바르지 않습니다."),
    PASSWORD_ERROR_COUNT_EXCEEDED(HttpStatus.BAD_REQUEST, "비밀번호 오류 횟수를 초과했습니다."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
