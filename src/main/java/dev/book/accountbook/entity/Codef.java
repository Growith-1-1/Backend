package dev.book.accountbook.entity;

import dev.book.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Codef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity user;
    private String bankCode;
    private String account;
    private String connectedId;

    public Codef(UserEntity user, String bankCode, String account, String connectedId) {
        this.user = user;
        this.bankCode = bankCode;
        this.account = account;
        this.connectedId = connectedId;
    }
}
