package dev.book.challenge.repository;

import dev.book.challenge.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long>, ChallengeJpaRepository {

    @Query("SELECT DISTINCT c FROM Challenge c JOIN FETCH c.creator JOIN FETCH c.challengeCategories cc JOIN FETCH cc.category where c.id=:id")
    Optional<Challenge> findWithCreatorById(Long id);

    @Query("SELECT c FROM Challenge c JOIN FETCH c.creator u JOIN FETCH c.challengeCategories cc JOIN FETCH cc.category where c.id=:id and u.id=:creatorId")
    Optional<Challenge> findByIdAndCreatorId(Long id, Long creatorId);

    @Query("SELECT c FROM Challenge c WHERE c.endDate < :today AND c.status <> 'COMPLETED'")
    List<Challenge> findChallengesToUpdate(@Param("today") LocalDate today);

    @Query("SELECT c FROM Challenge c JOIN FETCH c.challengeCategories WHERE c.id=:challengeId")
    Optional<Challenge> findByIdJoinCategory(Long challengeId);

}
