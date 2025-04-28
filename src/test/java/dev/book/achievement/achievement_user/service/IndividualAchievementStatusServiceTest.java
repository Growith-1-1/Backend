package dev.book.achievement.achievement_user.service;

import dev.book.achievement.achievement_user.dto.event.*;
import dev.book.achievement.achievement_user.entity.IndividualAchievementStatus;
import dev.book.achievement.achievement_user.repository.IndividualAchievementStatusRepository;
import dev.book.achievement.service.AchievementService;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.global.config.security.jwt.JwtAuthenticationToken;
import dev.book.user.entity.UserEntity;
import dev.book.util.UserBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndividualAchievementStatusServiceTest {

    @Mock
    private AchievementService achievementService;

    @Mock
    private IndividualAchievementStatusRepository individualAchievementStatusRepository;

    @InjectMocks
    private IndividualAchievementStatusService individualAchievementStatusService;

    CustomUserDetails userDetails;


    @BeforeEach
    public void createUser(){
        UserEntity user = UserBuilder.of();
        ReflectionTestUtils.setField(user, "id", 1L);
        userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(userDetails, userDetails.getAuthorities()));
    }

    /*
    객체 생성
     */
    @NotNull
    private IndividualAchievementStatus getIndividualAchievementStatus() { //실제 객체
        IndividualAchievementStatus individualAchievementStatus = new IndividualAchievementStatus(userDetails.user());
        given(individualAchievementStatusRepository.findByUser(userDetails.user())).willReturn(Optional.of(individualAchievementStatus));
        return individualAchievementStatus;
    }

    @NotNull
    private IndividualAchievementStatus getMockIndividualAchievementStatus() { //Mock 객체
        IndividualAchievementStatus individualAchievementStatus = mock(IndividualAchievementStatus.class);
        given(individualAchievementStatusRepository.findByUser(userDetails.user())).willReturn(Optional.of(individualAchievementStatus));
        return individualAchievementStatus;
    }

    /*
    then 사용
     */
    private void checkNotGetAchievement() { //달성한 업적이 없음
        verifyNoInteractions(achievementService);
        verify(achievementService, never()).saveAchievement(anyLong(), anyLong());
    }

    private void getAchievement(long num) {
        verify(achievementService).saveAchievement(num, userDetails.user().getId());
    }

    /*
    로그인 업적
     */
    private IndividualAchievementStatus setConsecutiveLogin(long value) {
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        individualAchievementStatus.loginToday(false);
        ReflectionTestUtils.setField(individualAchievementStatus, "consecutiveLogins", value);
        return individualAchievementStatus;
    }

    @Test
    @DisplayName("연속적으로 7번 로그인을 하여 업적을 달성한다.")
    void sevenTimesContinuousLogin() {
        //given
        IndividualAchievementStatus individualAchievementStatus = setConsecutiveLogin(6L);

        //when
        individualAchievementStatusService.deterMineContinuousLogin(userDetails.user());

        //then
        assertThat(individualAchievementStatus.isLoginToday()).isTrue();
        getAchievement(7L);
    }

    @Test
    @DisplayName("연속적으로 30번 로그인을 하여 업적을 달성한다.")
    void thirtyTimesContinuousLogin() {
        //given
        IndividualAchievementStatus individualAchievementStatus = setConsecutiveLogin(29L);

        //when
        individualAchievementStatusService.deterMineContinuousLogin(userDetails.user());

        //then
        assertThat(individualAchievementStatus.isLoginToday()).isTrue();
        getAchievement(8L);
    }

    @Test
    @DisplayName("연속적으로 2번 로그인을 하면 업적에 달성하지 않는다.")
    void SecondTimesContinuousLogin() {
        //given
        setConsecutiveLogin(1L);

        //when
        individualAchievementStatusService.deterMineContinuousLogin(userDetails.user());

        //then
        checkNotGetAchievement();
    }

    @Test
    @DisplayName("이미 로그인을 하여 업적 업데이트가 진행되지 않는다.")
    void alreadyContinuousLogin() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        individualAchievementStatus.loginToday(true);

        //when
        individualAchievementStatusService.deterMineContinuousLogin(userDetails.user());

        //then
        checkNotGetAchievement();
    }

    /*
    성공한 챌린지 업적
     */
    @NotNull
    private CompleteChallengeEvent getCompleteChallengeEvent(int value) {
        IndividualAchievementStatus individualAchievementStatus = getMockIndividualAchievementStatus();
        CompleteChallengeEvent event = new CompleteChallengeEvent(userDetails.user());
        given(individualAchievementStatus.plusCompleteChallenge()).willReturn(value);
        return event;
    }

    @Test
    @DisplayName("완료한 챌린지가 1개라면 업적에 달성한다.")
    void oneCompleteChallenge() {
        //given
        CompleteChallengeEvent event = getCompleteChallengeEvent(1);

        //when
        individualAchievementStatusService.plusCompleteChallenge(event);

        //then
        getAchievement(1L);
    }

    @Test
    @DisplayName("완료한 챌린지가 5개라면 업적에 달성한다.")
    void fiveCompleteChallenge() {
        //given
        CompleteChallengeEvent event = getCompleteChallengeEvent(5);

        //when
        individualAchievementStatusService.plusCompleteChallenge(event);

        //then
        getAchievement(2L);
    }

    @Test
    @DisplayName("완료한 챌린지가 10개라면 업적에 달성한다.")
    void tenCompleteChallenge() {
        //given
        CompleteChallengeEvent event = getCompleteChallengeEvent(10);

        //when
        individualAchievementStatusService.plusCompleteChallenge(event);

        //then
        getAchievement(3L);
    }

    @Test
    @DisplayName("완료한 챌린지가 2개라면 업적에 달성하지 않는다.")
    void twoCompleteChallenge() {
        //given
        CompleteChallengeEvent event = getCompleteChallengeEvent(2);

        //when
        individualAchievementStatusService.plusCompleteChallenge(event);

        //then
        checkNotGetAchievement();
    }

    /*
    실패한 업적
     */
    @NotNull
    private FailChallengeEvent getFailChallengeEvent(int value) {
        IndividualAchievementStatus individualAchievementStatus = getMockIndividualAchievementStatus();
        FailChallengeEvent event = new FailChallengeEvent(userDetails.user());
        given(individualAchievementStatus.plusFailChallenge()).willReturn(value);
        return event;
    }

    @Test
    @DisplayName("실패한 챌린지가 1개라면 업적에 달성한다.")
    void oneFailChallenge() {
        //given
        FailChallengeEvent event = getFailChallengeEvent(1);

        //when
        individualAchievementStatusService.plusFailChallenge(event);

        //then
        getAchievement(4L);
    }

    @Test
    @DisplayName("실패한 챌린지가 1개 이상 이라면 업적에 달성하지 않는다.")
    void oneMoreFailChallenge() {
        //given
        FailChallengeEvent event = getFailChallengeEvent(2);

        //when
        individualAchievementStatusService.plusFailChallenge(event);

        //then
        checkNotGetAchievement();
    }

    /*
    챌린지 생성 업적
     */
    @NotNull
    private CreateChallengeEvent getCreateChallengeEvent(int value) {
        IndividualAchievementStatus individualAchievementStatus = getMockIndividualAchievementStatus();
        CreateChallengeEvent event = new CreateChallengeEvent(userDetails.user());
        given(individualAchievementStatus.plusCreateChallenge()).willReturn(value);
        return event;
    }

    @Test
    @DisplayName("챌린지를 1개 생성하면 업적에 달성한다.")
    void oneCreateChallenge() {
        //given
        CreateChallengeEvent event = getCreateChallengeEvent(1);

        //when
        individualAchievementStatusService.plusCreateChallenge(event);

        //then
        getAchievement(5L);
    }

    @Test
    @DisplayName("챌린지를 5개 생성하면 업적에 달성한다.")
    void fiveCreateChallenge() {
        //given
        CreateChallengeEvent event = getCreateChallengeEvent(5);

        //when
        individualAchievementStatusService.plusCreateChallenge(event);

        //then
        getAchievement(6L);
    }

    @Test
    @DisplayName("챌린지를 1,5개 외의 개수만큼 생성하면 업적에 달성하지 않는다.")
    void sixCreateChallenge() {
        //given
        CreateChallengeEvent event = getCreateChallengeEvent(6);

        //when
        individualAchievementStatusService.plusCreateChallenge(event);

        //then
        checkNotGetAchievement();
    }

    /*
    소비 분석 업적
     */
    @NotNull
    private CheckSpendAnalysisEvent getCheckSpendAnalysisEvent(long value) {
        IndividualAchievementStatus individualAchievementStatus = getMockIndividualAchievementStatus();
        CheckSpendAnalysisEvent event = new CheckSpendAnalysisEvent(userDetails.user());
        given(individualAchievementStatus.plusCheckSpendAnalysis()).willReturn(value);
        return event;
    }

    @Test
    @DisplayName("소비 분석을 처음으로 진행하여 업적에 달성한다.")
    void oneCheckSpendAnalysis() {
        //given
        CheckSpendAnalysisEvent event = getCheckSpendAnalysisEvent(1);

        //when
        individualAchievementStatusService.plusCheckSpendAnalysis(event);

        //then
        getAchievement(9L);
    }

    @Test
    @DisplayName("소비 분석을 처음으로 이후에 진행하면 업적에 달성하지 않는다.")
    void oneMoreCheckSpendAnalysis() {
        //given
        CheckSpendAnalysisEvent event = getCheckSpendAnalysisEvent(2);

        //when
        individualAchievementStatusService.plusCheckSpendAnalysis(event);

        //then
        checkNotGetAchievement();
    }

    /*
    수입 등록 업적
     */
    @Test
    @DisplayName("첫 정기적인 수입을 작성하면 업적에 달성한다.")
    void setCreateFirstIncomeTrue() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getMockIndividualAchievementStatus();
        given(individualAchievementStatus.isCreateFirstIncome()).willReturn(false);
        CreateFirstIncomeEvent event = new CreateFirstIncomeEvent(userDetails.user());

        //when
        individualAchievementStatusService.setCreateFirstIncomeTrue(event);

        //then
        getAchievement(10L);
    }

    @Test
    @DisplayName("이미 업적에 달성했다면 정기적인 수입을 작성해도 업적에 달성하지 않는다.")
    void alreadyCreateFirstIncomeTrue() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getMockIndividualAchievementStatus();
        given(individualAchievementStatus.isCreateFirstIncome()).willReturn(true);
        CreateFirstIncomeEvent event = new CreateFirstIncomeEvent(userDetails.user());

        //when
        individualAchievementStatusService.setCreateFirstIncomeTrue(event);

        //then
        checkNotGetAchievement();
    }

    /*
    Week(주) 절약 업적
     */
    @Test
    @DisplayName("Week(주)의 절약 정도가 5% 이상이면 업적에 달성한다.")
    void fivePercentSaveAccomplishmentOfWeek() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        SaveConsumeOfWeekEvent event = new SaveConsumeOfWeekEvent(userDetails.user(), 5.0);

        //when
        individualAchievementStatusService.achieveSaveAccomplishmentOfWeek(event);

        //then
        assertThat(individualAchievementStatus.isSaveFivePercentOnLastWeek()).isTrue();
        getAchievement(11L);
    }

    @Test
    @DisplayName("Week(주)의 절약 정도가 10% 이상이면 업적 두 개에 달성한다.")
    void tenPercentSaveAccomplishmentOfWeek() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        SaveConsumeOfWeekEvent event = new SaveConsumeOfWeekEvent(userDetails.user(), 10.0);

        //when
        individualAchievementStatusService.achieveSaveAccomplishmentOfWeek(event);

        //then
        assertThat(individualAchievementStatus.isSaveFivePercentOnLastWeek()).isTrue();
        assertThat(individualAchievementStatus.isSaveTenPercentOnLastWeek()).isTrue();
        getAchievement(11L);
        getAchievement(12L);
    }

    @Test
    @DisplayName("5% 업적 달성 후 Week(주)의 절약 정도가 10% 이상이면 업적에 달성한다.")
    void afterFiveAndTenPercentSaveAccomplishmentOfWeek() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        individualAchievementStatus.setSaveFivePercentOnLastWeek(); //5% 업적 달성
        SaveConsumeOfWeekEvent event = new SaveConsumeOfWeekEvent(userDetails.user(), 10.0);

        //when
        individualAchievementStatusService.achieveSaveAccomplishmentOfWeek(event);

        //then
        assertThat(individualAchievementStatus.isSaveTenPercentOnLastWeek()).isTrue();
        verify(achievementService, never()).saveAchievement(11L, userDetails.user().getId());
        getAchievement(12L);
    }

    /*
    예산 계획 생성 업적
     */
    @NotNull
    private CreateBudgetEvent getCreateBudgetEvent(int value) {
        IndividualAchievementStatus individualAchievementStatus = getMockIndividualAchievementStatus();
        CreateBudgetEvent event = new CreateBudgetEvent(userDetails.user());
        given(individualAchievementStatus.plusCreateBudget()).willReturn(value);
        return event;
    }

    @Test
    @DisplayName("예산 계획을 1번 세웠을 경우 업적에 달성한다")
    void oneCreateBudget() {
        //given
        CreateBudgetEvent event = getCreateBudgetEvent(1);

        //when
        individualAchievementStatusService.plusCreateBudget(event);

        //then
        getAchievement(14L);
    }

    @Test
    @DisplayName("예산 계획을 3번 세웠을 경우 업적에 달성한다")
    void threeCreateBudget() {
        //given
        CreateBudgetEvent event = getCreateBudgetEvent(3);

        //when
        individualAchievementStatusService.plusCreateBudget(event);

        //then
        getAchievement(15L);
    }

    @Test
    @DisplayName("예산 계획을 1,3번 외의 개수로 세웠을 경우 업적에 달성하지 않는다.")
    void fiveCreateBudget() {
        //given
        CreateBudgetEvent event = getCreateBudgetEvent(5);

        //when
        individualAchievementStatusService.plusCreateBudget(event);

        //then
        checkNotGetAchievement();
    }

    /*
    예산 계획 성공 업적
     */
    @Test
    @DisplayName("예산 계획을 1번 성공했을 경우 업적에 달성한다.")
    void oneSuccessBudgetPlan() {
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        SuccessBudgetPlanEvent event = new SuccessBudgetPlanEvent(userDetails.user());

        //then
        individualAchievementStatusService.plusSuccessBudgetPlan(event);

        //when
        getAchievement(16L);
        assertThat(individualAchievementStatus.isSuccessBudgetPlanLastMonth()).isTrue();
    }

    @Test
    @DisplayName("예산 계획을 3번 성공했을 경우 업적에 달성한다.")
    void thirdSuccessBudgetPlan() {
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        SuccessBudgetPlanEvent event = new SuccessBudgetPlanEvent(userDetails.user());
        individualAchievementStatus.setSuccessBudgetPlanLastMonth(true);
        for (int i=0; i<2; i++) individualAchievementStatus.plusSuccessBudgetPlan();

        //then
        individualAchievementStatusService.plusSuccessBudgetPlan(event);

        //when
        getAchievement(17L);
        assertThat(individualAchievementStatus.isSuccessBudgetPlanLastMonth()).isTrue();
    }

    @Test
    @DisplayName("예산 계획을 저번 달에 성공하지 못했다면 기록이 초기화되어 업적에 달성하지 않는다.")
    void resetSuccessBudgetPlan() {
        IndividualAchievementStatus individualAchievementStatus = getMockIndividualAchievementStatus();
        SuccessBudgetPlanEvent event = new SuccessBudgetPlanEvent(userDetails.user());
        given(individualAchievementStatus.isSuccessBudgetPlanLastMonth()).willReturn(false);

        //then
        individualAchievementStatusService.plusSuccessBudgetPlan(event);

        //when
        verify(individualAchievementStatus).resetSuccessBudgetPlan();
        verify(individualAchievementStatus).setSuccessBudgetPlanLastMonth(true);
        checkNotGetAchievement();
    }

    /*
    과소비 알림 업적
     */
    @Test
    @DisplayName("과소비 알림을 1번 받으면 업적에 달성한다.")
    void oneGetWarningBudget() {
        //given
        getIndividualAchievementStatus();
        GetWarningBudgetEvent event = new GetWarningBudgetEvent(userDetails.user());

        //when
        individualAchievementStatusService.plusGetWarningBudget(event);

        //then
        getAchievement(18L);
    }

    @Test
    @DisplayName("과소비 알림을 1번 이상 받으면 업적에 달성하지 않는다.")
    void oneMoreGetWarningBudget() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        individualAchievementStatus.plusGetWarningBudget(); //이미 1번 받았음
        GetWarningBudgetEvent event = new GetWarningBudgetEvent(userDetails.user());

        //when
        individualAchievementStatusService.plusGetWarningBudget(event);

        //then
        checkNotGetAchievement();
    }

    /*
    예산 계획 절약 업적
     */
    @Test
    @DisplayName("예산 계획보다 5% 절약했을 경우 업적에 달성한다.")
    void fivePercentSaveAccomplishmentFromBudget() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        SaveConsumeFromBudgetEvent event = new SaveConsumeFromBudgetEvent(userDetails.user(), 5.0);

        //when
        individualAchievementStatusService.achieveSaveAccomplishmentFromBudget(event);

        //then
        assertThat(individualAchievementStatus.isSaveFivePercentFromBudget()).isTrue();
        getAchievement(19L);
    }

    @Test
    @DisplayName("예산 계획보다 10% 절약했을 경우 업적 두 개에 달성한다.")
    void tenPercentSaveAccomplishmentFromBudget() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        SaveConsumeFromBudgetEvent event = new SaveConsumeFromBudgetEvent(userDetails.user(), 10.0);

        //when
        individualAchievementStatusService.achieveSaveAccomplishmentFromBudget(event);

        //then
        assertThat(individualAchievementStatus.isSaveFivePercentFromBudget()).isTrue();
        assertThat(individualAchievementStatus.isSaveTenPercentFromBudget()).isTrue();
        getAchievement(19L);
        getAchievement(20L);
    }

    @Test
    @DisplayName("5% 업적 달성 후 예산 계획보다 10% 절약했을 경우 업적에 달성한다.")
    void afterFivePercentSaveAccomplishmentFromBudget() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        individualAchievementStatus.setSaveFivePercentFromBudget(); //5% 업적 이미 달성
        SaveConsumeFromBudgetEvent event = new SaveConsumeFromBudgetEvent(userDetails.user(), 10.0);

        //when
        individualAchievementStatusService.achieveSaveAccomplishmentFromBudget(event);

        //then
        assertThat(individualAchievementStatus.isSaveTenPercentFromBudget()).isTrue();
        getAchievement(20L);
    }

    @Test
    @DisplayName("예산 계획보다 20% 절약했을 경우 업적 두 개에 달성한다.")
    void twentyPercentSaveAccomplishmentFromBudget() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        SaveConsumeFromBudgetEvent event = new SaveConsumeFromBudgetEvent(userDetails.user(), 20.0);

        //when
        individualAchievementStatusService.achieveSaveAccomplishmentFromBudget(event);

        //then
        assertThat(individualAchievementStatus.isSaveFivePercentFromBudget()).isTrue();
        assertThat(individualAchievementStatus.isSaveTenPercentFromBudget()).isTrue();
        getAchievement(19L);
        getAchievement(20L);
    }

    /*
    친구 서비스 초대 업적
     */
    @Test
    @DisplayName("친구를 서비스에 1번 초대할 경우 업적에 달성한다.")
    void oneInviteFriendToService() {
        //given
        getIndividualAchievementStatus();
        InviteFriendToServiceEvent event = new InviteFriendToServiceEvent(userDetails.user());

        //when
        individualAchievementStatusService.plusInviteFriendToService(event);

        //then
        getAchievement(21L);
    }

    @Test
    @DisplayName("친구를 서비스에 3번 초대할 경우 업적에 달성한다.")
    void threeInviteFriendToService() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        for (int i=0; i<2; i++) individualAchievementStatus.plusInviteFriendToService();
        InviteFriendToServiceEvent event = new InviteFriendToServiceEvent(userDetails.user());

        //when
        individualAchievementStatusService.plusInviteFriendToService(event);

        //then
        getAchievement(22L);
    }

    @Test
    @DisplayName("친구를 서비스에 1,3번 외의 횟수로 초대할 경우 업적에 달성하지 않는다.")
    void twoInviteFriendToService() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        individualAchievementStatus.plusInviteFriendToService();
        InviteFriendToServiceEvent event = new InviteFriendToServiceEvent(userDetails.user());

        //when
        individualAchievementStatusService.plusInviteFriendToService(event);

        //then
        checkNotGetAchievement();
    }

    /*
    친구 챌린지 초대 업적
     */
    @Test
    @DisplayName("친구를 챌린지에 1번 초대할 경우 업적에 달성한다.")
    void oneInviteFriendToChallenge() {
        //given
        getIndividualAchievementStatus();
        InviteFriendToChallengeEvent event = new InviteFriendToChallengeEvent(userDetails.user());

        //when
        individualAchievementStatusService.plusInviteFriendToChallenge(event);

        //then
        getAchievement(23L);
    }

    @Test
    @DisplayName("친구를 챌린지에 1번 외의 횟수로 초대할 경우 업적에 달성하지 않는다.")
    void twoInviteFriendToChallenge() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        individualAchievementStatus.plusInviteFriendToChallenge();
        InviteFriendToChallengeEvent event = new InviteFriendToChallengeEvent(userDetails.user());

        //when
        individualAchievementStatusService.plusInviteFriendToChallenge(event);

        //then
        checkNotGetAchievement();
    }

    /*
    팁 공유 업적
     */
    @Test
    @DisplayName("팁을 1번 공유하면 업적에 달성한다.")
    void oneShareTips() {
        //given
        getIndividualAchievementStatus();
        ShareTipsEvent event = new ShareTipsEvent(userDetails.user());

        //when
        individualAchievementStatusService.plusShareTips(event);

        //then
        getAchievement(24L);
    }

    @Test
    @DisplayName("팁을 5번 공유하면 업적에 달성한다.")
    void threeShareTips() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        for (int i=0; i<4; i++) individualAchievementStatus.plusShareTips();
        ShareTipsEvent event = new ShareTipsEvent(userDetails.user());

        //when
        individualAchievementStatusService.plusShareTips(event);

        //then
        getAchievement(25L);
    }

    @Test
    @DisplayName("팁을 1,5번 외의 횟수로 공유하면 업적에 달성하지 않는다.")
    void twoShareTips() {
        //given
        IndividualAchievementStatus individualAchievementStatus = getIndividualAchievementStatus();
        individualAchievementStatus.plusShareTips();
        ShareTipsEvent event = new ShareTipsEvent(userDetails.user());

        //when
        individualAchievementStatusService.plusShareTips(event);

        //then
        checkNotGetAchievement();
    }

    /*
    삭제
     */
    @Test
    @DisplayName("개인 업적 상태를 삭제합니다.")
    void deleteIndividualAchievementStatus() {
        //when
        individualAchievementStatusService.deleteIndividualAchievementStatus(userDetails.user());

        //then
        verify(individualAchievementStatusRepository).deleteByUser(userDetails.user());
    }

    /*
    조회 에러
     */
    @Test
    @DisplayName("개인 업적 상태 테이블이 존재하지 않을 경우 새로 생성한다.")
    void getNewIndividualAchievementStatus(){
        //given
        given(individualAchievementStatusRepository.findByUser(userDetails.user())).willReturn(Optional.empty());
        IndividualAchievementStatus individualAchievementStatus = new IndividualAchievementStatus(userDetails.user());
        given(individualAchievementStatusRepository.save(any())).willReturn(individualAchievementStatus);

        //when
        individualAchievementStatusService.deterMineContinuousLogin(userDetails.user());

        //then
        verify(individualAchievementStatusRepository).findByUser(userDetails.user());
        verify(individualAchievementStatusRepository).save(any(IndividualAchievementStatus.class));
    }

    @Test
    @DisplayName("개인 업적 상태 테이블이 존재할 경우 반환한다.")
    void getExistedIndividualAchievementStatus(){
        //given
        IndividualAchievementStatus individualAchievementStatus = new IndividualAchievementStatus(userDetails.user());
        given(individualAchievementStatusRepository.findByUser(userDetails.user())).willReturn(Optional.of(individualAchievementStatus));

        //when
        individualAchievementStatusService.deterMineContinuousLogin(userDetails.user());

        //then
        verify(individualAchievementStatusRepository).findByUser(userDetails.user());
        verify(individualAchievementStatusRepository, never()).save(any(IndividualAchievementStatus.class));
    }
}