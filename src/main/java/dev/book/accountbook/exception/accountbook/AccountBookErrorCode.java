package dev.book.accountbook.exception.accountbook;

import dev.book.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AccountBookErrorCode implements ErrorCode {
    NOT_FOUND_SPEND(HttpStatus.NOT_FOUND, "존재하지 않는 소비내역입니다."),
    NOT_FOUND_INCOME(HttpStatus.NOT_FOUND, "존재하지 않는 수입내역입니다."),
    NOT_FOUND_CATEGORY(HttpStatus.NOT_FOUND,"존재하지 않는 카테고리입니다."),
    NOT_FOUND_BUDGET(HttpStatus.NOT_FOUND, "존재하지 않는 예산입니다."),
    DUPLICATE_BUDGET(HttpStatus.CONFLICT, "이미 예산이 등록되어 있습니다."),;

    private final HttpStatus status;
    private final String message;
}
