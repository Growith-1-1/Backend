package dev.book.challenge.scheduler;

import dev.book.accountbook.repository.AccountBookRepository;
import dev.book.accountbook.type.CategoryType;
import dev.book.achievement.achievement_user.dto.event.CompleteChallengeEvent;
import dev.book.achievement.achievement_user.dto.event.FailChallengeEvent;
import dev.book.challenge.ChallengeCategory;
import dev.book.challenge.entity.Challenge;
import dev.book.challenge.repository.ChallengeRepository;
import dev.book.challenge.type.Status;
import dev.book.challenge.user_challenge.entity.UserChallenge;
import dev.book.challenge.user_challenge.repository.UserChallengeRepository;
import dev.book.global.entity.Category;
import dev.book.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChallengeScheduler {
    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final AccountBookRepository accountBookRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Scheduled(cron = "0 0 0 * * *") //매일 자정에 챌린지 close
    @Transactional
    public void updateChallengeStatus() {
        log.info("챌린지 상태 업데이트가 시작 되었습니다.");
        LocalDate today = LocalDate.now();

        List<Status> startStatuses = List.of(Status.RECRUITING, Status.RECRUITED);
        // 현재시간 기준으로 챌린지 모집 기간이 지났고 모집중, 모집완료된 챌린지를 불러와 진행중 상태로 변경
        List<Challenge> startChallenges = challengeRepository.findChallengesToStart(today, startStatuses);
        for (Challenge challenge : startChallenges) {
            challenge.startChallenge();
        }
        // 현재시간 기준으로 종료 되야할 챌린지를 불러온다.
        List<Challenge> completedChallenges = challengeRepository.findChallengesToUpdate(today);
        // 불러온 챌린지들의 상태를 바꾸고 그 챌린지에 유저들의 성공 여부를 판단한다.
        for (Challenge challenge : completedChallenges) {
            challenge.completeChallenge();
            List<UserChallenge> userChallenges = userChallengeRepository.findUsersByChallengeId(challenge.getId());
            //챌린지의 기간 동안의 내역을 가져와서 챌린지의 한도 내역과 비교
            List<Category> categoryList = ChallengeCategory.getCategoryList(challenge.getChallengeCategories());
            userChallenges.forEach(userChallenge -> {
                UserEntity user = userChallenge.getUser();
                Integer sumOfCategories = accountBookRepository.sumSpendingInCategories(
                        user.getId(), CategoryType.SPEND, categoryList, challenge.getStartDate(), challenge.getEndDate());
                if (sumOfCategories <= challenge.getAmount()) {
                    user.plusCompleteChallenge();
                    user.plusSavings(challenge.getAmount() - sumOfCategories);
                    userChallenge.success();
                    eventPublisher.publishEvent(new CompleteChallengeEvent(user));
                } else {
                    eventPublisher.publishEvent(new FailChallengeEvent(user));
                }
                user.plusFinishedChallenge();
            });
        }
    }
}
