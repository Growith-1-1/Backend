package dev.book.challenge.user_challenge.repository;

import dev.book.challenge.entity.Challenge;
import dev.book.challenge.user_challenge.entity.UserChallenge;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {

    boolean existsByUserIdAndChallengeId(Long id, Long challengeId);

    Optional<UserChallenge> findByUserIdAndChallengeId(Long id, Long challengeId);

    void deleteByUserIdAndChallengeId(Long userId, Long challengeId);

    @Query("SELECT uc.user.id FROM UserChallenge uc WHERE uc.challenge.id=:challengeId")
    List<Long> findUserIdByChallengeId(Long challengeId);

    @Query("""
                SELECT DISTINCT uc.challenge
                FROM UserChallenge uc
                JOIN uc.challenge c
                JOIN c.challengeCategories cc
                WHERE uc.user.id = :userId
                  AND :categoryId = cc.category.id
                  AND :spendDate BETWEEN c.startDate AND c.endDate
            """)
    List<Challenge> findChallengesByUserAndDate(Long userId, Long categoryId, LocalDate spendDate);

    @Query("SELECT uc FROM UserChallenge uc JOIN UserEntity u ON uc.user.id=u.id JOIN Challenge c ON uc.challenge.id=c.id WHERE uc.challenge.id=:challengeId")
    List<UserChallenge> findUsersByChallengeId(Long challengeId);

    @Query("""
            SELECT uc FROM UserChallenge uc
            JOIN FETCH uc.challenge c
            JOIN FETCH c.challengeCategories cc
            JOIN FETCH cc.category ca
            WHERE uc.user.id=:id
            """)
    List<UserChallenge> findChallengeByUserId(Long id, Pageable pageable);
}
