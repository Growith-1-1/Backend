package dev.book.accountbook.exception.codef;

import dev.book.global.exception.CustomErrorException;
import lombok.Getter;

@Getter
public class CodefErrorException extends CustomErrorException {
    private final CodefErrorCode errorCode;
    private final Object additionalData;

    public CodefErrorException(CodefErrorCode errorCode) {
        super(errorCode.getMessage(), errorCode);
        this.errorCode = errorCode;
        this.additionalData = null;
    }

    public CodefErrorException(CodefErrorCode errorCode, Object additionalData) {
        super(errorCode.getMessage(), errorCode);
        this.errorCode = errorCode;
        this.additionalData = additionalData;
    }
}
