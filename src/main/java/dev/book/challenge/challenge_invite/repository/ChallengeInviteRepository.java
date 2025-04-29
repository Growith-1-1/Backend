package dev.book.challenge.challenge_invite.repository;

import dev.book.challenge.challenge_invite.entity.ChallengeInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeInviteRepository extends JpaRepository<ChallengeInvite, Long> {

    boolean existsByInviteUserIdAndChallengeId(Long inviteUserId, Long challengeId);

    @Query("SELECT c FROM ChallengeInvite c JOIN FETCH c.challenge JOIN FETCH c.requestUser JOIN FETCH  c.inviteUser WHERE c.inviteUser.id=:id")
    List<ChallengeInvite> findAllByInviteUserId(Long id);

    @Query("SELECT c FROM ChallengeInvite c JOIN FETCH c.challenge JOIN FETCH c.requestUser JOIN FETCH  c.inviteUser WHERE c.id=:inviteId AND c.inviteUser.id=:inviteUserId")
    Optional<ChallengeInvite> findByIdAndInviteUserId(Long inviteId, Long inviteUserId);
}
