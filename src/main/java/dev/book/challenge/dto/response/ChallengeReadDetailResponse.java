package dev.book.challenge.dto.response;

import dev.book.challenge.entity.Challenge;
import dev.book.challenge.type.Release;
import dev.book.challenge.type.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ChallengeReadDetailResponse(Long id, String creator, String title, Release release, Integer amount,
                                          Integer capacity, List<CategoryDto> challengeCategory, Status status,
                                          LocalDate startDate,
                                          LocalDate endDate, LocalDateTime createDate,
                                          LocalDateTime modifyDate) {

    public static ChallengeReadDetailResponse fromEntity(Challenge challenge) {
        return new ChallengeReadDetailResponse(challenge.getId(),
                challenge.getCreator().getName(),
                challenge.getTitle(),
                challenge.getRelease(),
                challenge.getAmount(),
                challenge.getCapacity(),
                challenge.getChallengeCategories().stream().map(challengeCategory -> new CategoryDto(challengeCategory.getCategory().getKorean())).toList(),
                challenge.getStatus(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                challenge.getCreatedAt(),
                challenge.getUpdatedAt());
    }

    public record CategoryDto(String name) {
    }
}
