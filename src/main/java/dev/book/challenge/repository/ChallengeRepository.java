package dev.book.challenge.repository;

import dev.book.challenge.dto.response.ChallengeReadResponse;
import dev.book.challenge.dto.response.ChallengeTopResponse;
import dev.book.challenge.entity.Challenge;
import dev.book.challenge.type.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long>, ChallengeJpaRepository {

    @Query("SELECT DISTINCT c FROM Challenge c JOIN FETCH c.creator JOIN FETCH c.challengeCategories cc JOIN FETCH cc.category where c.id=:id")
    Optional<Challenge> findWithCreatorById(Long id);

    @Query("SELECT DISTINCT c FROM Challenge c JOIN FETCH c.creator u JOIN FETCH c.challengeCategories cc JOIN FETCH cc.category where c.id=:id and u.id=:creatorId")
    Optional<Challenge> findByIdAndCreatorId(Long id, Long creatorId);

    @Query("SELECT c FROM Challenge c WHERE c.endDate < :today AND c.status <> 'COMPLETED'")
    List<Challenge> findChallengesToUpdate(@Param("today") LocalDate today);

    @Query("SELECT c FROM Challenge c JOIN FETCH c.challengeCategories WHERE c.id=:challengeId")
    Optional<Challenge> findByIdJoinCategory(Long challengeId);

    @Query("""
                SELECT new dev.book.challenge.dto.response.ChallengeTopResponse(
                    c.id, c.title, c.capacity,c.currentCapacity,c.status
                )
                FROM Challenge c
                WHERE c.status = 'RECRUITING'
                AND c.release ='PUBLIC'
            """)
    List<ChallengeTopResponse> findTopChallenge(Pageable pageable);

    @Query("""
                SELECT new dev.book.challenge.dto.response.ChallengeReadResponse(
                    c.id, c.title, c.capacity,c.currentCapacity,c.status
                )
                FROM Challenge c
                WHERE c.status = 'RECRUITING'
                AND  c.release = 'PUBLIC'
                AND c.createdAt BETWEEN :startDateTime AND :endDateTime
            """)
    List<ChallengeReadResponse> findNewChallenge(Pageable pageable, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT c FROM Challenge c WHERE c.id = :id")
    Optional<Challenge> findByIdWithLock(Long id);

    @Query("SELECT c FROM Challenge c WHERE c.startDate = :today AND c.status IN :startStatuses")
    List<Challenge> findChallengesToStart(@Param("today") LocalDate today, @Param("startStatuses") List<Status> startStatuses);
}
