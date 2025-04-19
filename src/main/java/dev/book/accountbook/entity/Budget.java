package dev.book.accountbook.entity;

import dev.book.accountbook.type.Category;
import dev.book.global.entity.BaseTimeEntity;
import dev.book.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Budget extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int budgetLimit;
    @Enumerated(EnumType.STRING)
    private Category category;
    private int month;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BudgetAccountBook> budgetReferences = new ArrayList<>();

    public Budget(int budgetLimit, Category category, int month, UserEntity user) {
        this.budgetLimit = budgetLimit;
        this.category = category;
        this.month = month;
        this.user = user;
    }

    public void modifyBudget(int budgetLimit) {
        int date = LocalDate.now().getMonthValue();

        this.budgetLimit = budgetLimit;
        this.month = date;
    }
}
