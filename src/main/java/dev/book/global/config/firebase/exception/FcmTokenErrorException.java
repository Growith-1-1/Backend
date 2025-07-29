package dev.book.global.config.firebase.exception;

import dev.book.global.exception.CustomErrorException;
import lombok.Getter;

@Getter
public class FcmTokenErrorException extends CustomErrorException {
    private final FcmTokenErrorCode errorCode;
    private final Object additionalDate;

    public FcmTokenErrorException(FcmTokenErrorCode errorCode) {
        super(errorCode.getMessage(), errorCode);
        this.errorCode = errorCode;
        this.additionalDate = null;
    }

    public FcmTokenErrorException(FcmTokenErrorCode errorCode, Object additionalDate) {
        super(errorCode.getMessage() + " " + additionalDate, errorCode);
        this.errorCode = errorCode;
        this.additionalDate = additionalDate;
    }
}
