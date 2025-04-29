package dev.book.challenge.repository;

import dev.book.challenge.dto.response.ChallengeReadResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChallengeJpaRepository {
    Page<ChallengeReadResponse> search(String title, String text, Pageable pageable);
}
