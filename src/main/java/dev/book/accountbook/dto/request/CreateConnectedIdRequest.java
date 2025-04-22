package dev.book.accountbook.dto.request;

import dev.book.accountbook.entity.Codef;
import dev.book.accountbook.type.Bank;
import dev.book.user.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateConnectedIdRequest(
        @NotBlank
        @Schema(description = "은행 ID", example = "sample123")
        String id,
        @NotBlank
        @Schema(description = "은행 PASSWORD", example = "thisispassword1234")
        String password,
        @NotBlank
        @Schema(description = "은행", example = "KB", allowableValues = {
                "KDB", "IBK", "KB", "SH", "NH",
                "WOORI", "SC", "CITI", "DAEGU", "BUSAN",
                "GWANGJU", "JEJU", "JB", "GYEONGNAM", "SAEMAUL",
                "SHINHYUP", "KOREA_POST", "HANA", "SHINHAN", "KBANK"
        })
        Bank bank,
        @NotBlank
        @Schema(description = "계좌번호.", example = "12345600789101")
        String accountNumber) {
    public Codef toEntity(UserEntity user, String bankCode, String accountNumber, String connectedId) {

        return new Codef(user, bankCode, accountNumber, connectedId);
    }
}
