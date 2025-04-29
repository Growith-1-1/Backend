package dev.book.global.exception.util;

import dev.book.global.exception.CustomErrorException;
import lombok.Getter;

@Getter
public class AESErrorException extends CustomErrorException {
    private final AESErrorCode aesErrorCode;
    private final Object additionalDate;

    public AESErrorException(AESErrorCode aesErrorCode) {
        super(aesErrorCode.getMessage(), aesErrorCode);
        this.aesErrorCode = aesErrorCode;
        this.additionalDate = null;
    }

    public AESErrorException(AESErrorCode aesErrorCode, Object additionalDate) {
        super(aesErrorCode.getMessage(), aesErrorCode);
        this.aesErrorCode = aesErrorCode;
        this.additionalDate = additionalDate;
    }
}
