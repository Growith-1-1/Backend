package dev.book.accountbook.entity;

import dev.book.accountbook.type.CategoryType;
import dev.book.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TempAccountBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String memo;
    private int amount;

    @Enumerated(value = EnumType.STRING)
    private CategoryType type;

    private LocalDate occurredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public TempAccountBook(String title, String memo, int amount, CategoryType type, UserEntity user, LocalDate occurredAt) {
        this.title = title;
        this.memo = memo;
        this.amount = amount;
        this.type = type;
        this.user = user;
        this.occurredAt = occurredAt;
    }
}
