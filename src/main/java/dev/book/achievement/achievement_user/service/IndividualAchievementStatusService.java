package dev.book.achievement.achievement_user.service;

import dev.book.achievement.achievement_user.dto.event.*;
import dev.book.achievement.achievement_user.entity.IndividualAchievementStatus;
import dev.book.achievement.achievement_user.repository.IndividualAchievementStatusRepository;
import dev.book.achievement.service.AchievementService;
import dev.book.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class IndividualAchievementStatusService {

    private final IndividualAchievementStatusRepository individualAchievementStatusRepository;
    private final AchievementService achievementService;

    @Transactional
    public void deterMineContinuousLogin(UserEntity user) {
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(user);
        long continuous = plusConsecutiveLogins(achievementStatus);
        if (continuous > 0)
            saveConsecutiveLoginAchievement(continuous, user.getId());
    }

    private void saveConsecutiveLoginAchievement(long consecutiveLogins, long userId) {
        if (consecutiveLogins == 7L)
            achievementService.saveAchievement(7L, userId);
        if (consecutiveLogins == 30L)
            achievementService.saveAchievement(8L, userId);
    }
    
    private static long plusConsecutiveLogins(IndividualAchievementStatus achievementStatus) {
        boolean isLoginToday = achievementStatus.isLoginToday();
        long continuous;
        if (!isLoginToday) {
            continuous = achievementStatus.plusConsecutiveLogins();
            achievementStatus.loginToday(true);
        } else {
            continuous = -1;
        }
        return continuous;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void plusCompleteChallenge(CompleteChallengeEvent event) {
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(event.user());
        int completeChallenge = achievementStatus.plusCompleteChallenge();
        switch (completeChallenge) {
            case 1 ->
                achievementService.saveAchievement(1L, event.user().getId());
            case 5 ->
                achievementService.saveAchievement(2L, event.user().getId());
            case 10 ->
                achievementService.saveAchievement(3L, event.user().getId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void plusFailChallenge(FailChallengeEvent event) {
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(event.user());
        int failChallenge = achievementStatus.plusFailChallenge();
        if (failChallenge == 1) {
            achievementService.saveAchievement(4L, event.user().getId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void plusCreateChallenge(CreateChallengeEvent event) {
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(event.user());
        int createChallenge = achievementStatus.plusCreateChallenge();
        switch (createChallenge) {
            case 1->
                achievementService.saveAchievement(5L, event.user().getId());
            case 5->
                achievementService.saveAchievement(6L, event.user().getId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void plusCheckSpendAnalysis(CheckSpendAnalysisEvent event) {
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(event.user());
        long checkSpendAnalysis = achievementStatus.plusCheckSpendAnalysis();
        if (checkSpendAnalysis == 1L)
            achievementService.saveAchievement(9L, event.user().getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setCreateFirstIncomeTrue(CreateFirstIncomeEvent event) {
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(event.user());
        if (!achievementStatus.isCreateFirstIncome()){
            achievementStatus.setCreateFirstIncomeTrue();
            achievementService.saveAchievement(10L, event.user().getId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void achieveSaveAccomplishmentOfWeek(SaveConsumeOfWeekEvent event){
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(event.user());
        //절약 %가 상한선 이상이면서, 사전에 업적 달성한 적이 없는 경우를 판단
        if (event.saveRate() >= 10.0 && !achievementStatus.isSaveTenPercentOnLastWeek())
            saveTenPercentOnLastWeek(event.user().getId(), achievementStatus);
        //10% 절약 달성했다는 것은 5%도 당연히 달성한 업적
        if (event.saveRate() >= 5.0 && !achievementStatus.isSaveFivePercentOnLastWeek())
            saveFivePercentOnLastWeek(event.user().getId(), achievementStatus);
    }

    private void saveFivePercentOnLastWeek(Long userId, IndividualAchievementStatus achievementStatus) {
        achievementStatus.setSaveFivePercentOnLastWeek();
        achievementService.saveAchievement(11L, userId);
    }

    private void saveTenPercentOnLastWeek(Long userId, IndividualAchievementStatus achievementStatus) {
        achievementStatus.setSaveTenPercentOnLastWeek();
        achievementService.saveAchievement(12L, userId);
    }

    //미구현 부분
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void plusConsecutiveNoSpend(ConsecutiveNoSpendEvent event) {
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(event.user());
        int consecutiveNoSpend = achievementStatus.plusConsecutiveNoSpend();
        if (consecutiveNoSpend == 3) {
            achievementService.saveAchievement(13L, event.user().getId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void plusCreateBudget(CreateBudgetEvent event) {
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(event.user());
        int createBudget = achievementStatus.plusCreateBudget();
        switch (createBudget) {
            case 1->
                achievementService.saveAchievement(14L, event.user().getId());
            case 3->
                achievementService.saveAchievement(15L, event.user().getId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void plusSuccessBudgetPlan(SuccessBudgetPlanEvent event) {
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(event.user());
        if (!achievementStatus.isSuccessBudgetPlanLastMonth()){
            achievementStatus.resetSuccessBudgetPlan(); //저번달에 성공하지 못했으면 0으로 초기화
        }
        int successBudgetPlan = achievementStatus.plusSuccessBudgetPlan();
        achievementStatus.setSuccessBudgetPlanLastMonth(true);
        switch (successBudgetPlan) {
            case 1->
                achievementService.saveAchievement(16L, event.user().getId());
            case 3->
                achievementService.saveAchievement(17L, event.user().getId());
        }
    }

    public void didntHaveBudgetPlan(UserEntity user){
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(user);
        achievementStatus.resetSuccessBudgetPlan();
        achievementStatus.setSuccessBudgetPlanLastMonth(false);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void plusGetWarningBudget(GetWarningBudgetEvent event) {
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(event.user());
        int getWarningBudget = achievementStatus.plusGetWarningBudget();
        if (getWarningBudget == 1) achievementService.saveAchievement(18L, event.user().getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void achieveSaveAccomplishmentFromBudget(SaveConsumeFromBudgetEvent event){
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(event.user());
        //절약 %가 상한선 이상이면서, 사전에 업적 달성한 적이 없는 경우를 판단
        if (event.saveRate() >= 10.0 && !achievementStatus.isSaveTenPercentFromBudget())
            saveTenPercentFromBudget(event.user().getId(), achievementStatus);
        if (event.saveRate() >= 5.0 && !achievementStatus.isSaveFivePercentFromBudget())
            saveFivePercentFromBudget(event.user().getId(), achievementStatus);
    }

    public void saveFivePercentFromBudget(Long userId, IndividualAchievementStatus achievementStatus) {
        achievementStatus.setSaveFivePercentFromBudget();
        achievementService.saveAchievement(19L, userId);
    }

    public void saveTenPercentFromBudget(Long userId, IndividualAchievementStatus achievementStatus) {
        achievementStatus.setSaveTenPercentFromBudget();
        achievementService.saveAchievement(20L, userId);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void plusInviteFriendToService(InviteFriendToServiceEvent event) {
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(event.user());
        int inviteFriendToService = achievementStatus.plusInviteFriendToService();
        switch (inviteFriendToService) {
            case 1->
                achievementService.saveAchievement(21L, event.user().getId());
            case 3->
                achievementService.saveAchievement(22L, event.user().getId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void plusInviteFriendToChallenge(InviteFriendToChallengeEvent event) {
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(event.user());
        int inviteFriendToChallenge = achievementStatus.plusInviteFriendToChallenge();
        if (inviteFriendToChallenge == 1) {
            achievementService.saveAchievement(23L, event.user().getId());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void plusShareTips(ShareTipsEvent event) {
        IndividualAchievementStatus achievementStatus = getIndividualAchievementStatus(event.user());
        int shareTips = achievementStatus.plusShareTips();
        switch (shareTips) {
            case 1->
                achievementService.saveAchievement(24L, event.user().getId());
            case 5->
                achievementService.saveAchievement(25L, event.user().getId());
        }
    }

    private IndividualAchievementStatus getIndividualAchievementStatus(UserEntity user) {
        return individualAchievementStatusRepository.findByUser(user)
                .orElseGet(() -> individualAchievementStatusRepository.save(new IndividualAchievementStatus(user)));
    }

    public void deleteIndividualAchievementStatus(UserEntity user) {
        individualAchievementStatusRepository.deleteByUser(user);
    }
}
