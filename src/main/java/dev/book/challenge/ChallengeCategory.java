package dev.book.challenge;

import dev.book.challenge.entity.Challenge;
import dev.book.global.entity.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    public ChallengeCategory(Challenge challenge, Category category) {
        this.challenge = challenge;
        this.category = category;
        challenge.getChallengeCategories().add(this);
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public static List<Category> getCategoryList(List<ChallengeCategory> challengeCategories) {
        return challengeCategories.stream().map(ChallengeCategory::getCategory).toList();

    }
}
