package dev.book.challenge.service;

import dev.book.challenge.entity.Challenge;
import dev.book.challenge.exception.ChallengeException;
import dev.book.challenge.repository.ChallengeRepository;
import dev.book.user.entity.UserEntity;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import static dev.book.challenge.exception.ErrorCode.*;
@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeLockService {
    private final ChallengeService challengeService;
    private final ChallengeRepository challengeRepository;

    public void participate(UserEntity user, Long challengeId) {

        int repeatCount = 10; // 반복 횟수
        while (repeatCount-- > 0) {
            try {
                challengeService.participateWithLock(user, challengeId);
                log.info("챌린지 참가 성공 : 사용자 ID={},챌린지 ID ={}", user.getId(), challengeId);
                return;
            } catch (OptimisticLockException | LockAcquisitionException | ObjectOptimisticLockingFailureException e) {

                Challenge challenge = challengeRepository.findById(challengeId).orElseThrow(() -> new ChallengeException(CHALLENGE_NOT_FOUND));

                if (challenge.getCurrentCapacity() >= challenge.getCapacity()) {
                    log.info("챌린지가 꽉 초과하였습니다., 최대인원 : {}", challenge.getCapacity());
                    throw new ChallengeException(CHALLENGE_CAPACITY_FULL);
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("챌린지참가 실패");
        throw new ChallengeException(CHALLENGE_PARTICIPANT_FAIL);
    }
}

