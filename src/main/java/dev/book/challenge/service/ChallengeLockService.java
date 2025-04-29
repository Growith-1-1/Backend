package dev.book.challenge.service;

import dev.book.challenge.entity.Challenge;
import dev.book.challenge.exception.ChallengeException;
import dev.book.challenge.repository.ChallengeRepository;
import dev.book.user.entity.UserEntity;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import static dev.book.challenge.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ChallengeLockService {
    private final ChallengeService challengeService;
    private final ChallengeRepository challengeRepository;


    public void participate(UserEntity user, Long challengeId) {

        int repeatCount = 10;
        while (repeatCount-- > 0) {
            try {
                challengeService.participateWithLock(user, challengeId);
                return;
            } catch (OptimisticLockException | LockAcquisitionException | ObjectOptimisticLockingFailureException ee) {

                Challenge challenge = challengeRepository.findById(challengeId).orElseThrow(() -> new ChallengeException(CHALLENGE_NOT_FOUND));

                if (challenge.getCurrentCapacity() >= challenge.getCapacity()) {
                    throw new ChallengeException(CHALLENGE_CAPACITY_FULL);
                }

            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        throw new ChallengeException(CHALLENGE_PARTICIPANT_FAIL);
    }
}

