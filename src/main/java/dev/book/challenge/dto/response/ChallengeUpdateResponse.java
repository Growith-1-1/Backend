package dev.book.challenge.dto.response;

import dev.book.challenge.entity.Challenge;
import dev.book.challenge.type.Release;
import dev.book.challenge.type.Status;
import dev.book.global.entity.Category;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ChallengeUpdateResponse(Long id, String title, String text, Release release,
                                      Integer amount,
                                      Integer capacity, List<CategoryDto> categories, Status status,
                                      LocalDate startDate, LocalDate endDate,
                                      LocalDateTime createDate,
                                      LocalDateTime modifyDate) {

    public static ChallengeUpdateResponse fromEntity(Challenge challenge, List<Category> categories) {
        return new ChallengeUpdateResponse(challenge.getId(), challenge.getTitle(),
                challenge.getText(), challenge.getRelease(), challenge.getAmount(),
                challenge.getCapacity(),
                challenge.getChallengeCategories().stream().map(challengeCategory -> new CategoryDto(challengeCategory.getCategory().getKorean())).toList(),


                challenge.getStatus(),
                challenge.getStartDate(), challenge.getEndDate(), challenge.getCreatedAt(),
                challenge.getUpdatedAt());
    }

    public record CategoryDto(String name) {
    }
}
