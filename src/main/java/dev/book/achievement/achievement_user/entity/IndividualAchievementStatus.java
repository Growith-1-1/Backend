package dev.book.achievement.achievement_user.entity;

import dev.book.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 업적 달성을 파악하기 위한 개인별 현황도
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IndividualAchievementStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne
    @JoinColumn(name="user_id")
    private UserEntity user;
    private int completeChallenge=0; //챌린지 달성
    private int failChallenge=0; //챌린지 실패
    private int createChallenge=0; //챌린지 생성

    private boolean isLoginYesterday = false;
    private boolean isLoginToday=false; //오늘 로그인 했는지

    private long consecutiveLogins=0; //연속적으로 로그인한 횟수

    private long checkSpendAnalysis=0; //소비 분석 탭을 확인한 횟수

    private boolean createFirstIncome=false;//첫번째 고정 수입 등록

    private boolean saveFivePercentOnLastWeek=false; //5% 절약

    private boolean saveTenPercentOnLastWeek=false; //10% 절약

    private int consecutiveNoSpend=0; //연속적으로 무지출한 횟수

    private int createBudget=0; //예산 계획 작성 횟수

    private boolean successBudgetPlanLastMonth = false; //저번달에 예산 계획을 지켰는지

    private int successBudgetPlan=0; //예산 계획 지킨 횟수

    private int getWarningBudget=0; //과소비 경고 받은 횟수

    private boolean saveFivePercentFromBudget=false;

    private boolean saveTenPercentFromBudget=false;

    private int inviteFriendToService=0; //서비스 내에 친구를 초대한 횟수

    private int inviteFriendToChallenge=0; //챌린지에 친구를 초대한 횟수

    private int shareTips=0;//소비 팁을 공유한 횟수

    public IndividualAchievementStatus(UserEntity user) {
        this.user = user;
    }
    public int plusCompleteChallenge(){
        return ++this.completeChallenge;
    }

    public int plusFailChallenge(){
        return ++this.failChallenge;
    }
    public int plusCreateChallenge(){
        return ++this.createChallenge;
    }

    public void loginYesterday(boolean isLoginYesterday){
        this.isLoginYesterday = isLoginYesterday;
    }
    public void loginToday(boolean isLoginToday){
        this.isLoginToday=isLoginToday;
    }

    public long plusConsecutiveLogins(){
        return ++this.consecutiveLogins;
    }

    public void resetConsecutiveLogins(){
        this.consecutiveLogins = 0;
    }

    public long plusCheckSpendAnalysis(){
        return ++this.checkSpendAnalysis;
    }

    public void setCreateFirstIncomeTrue(){
        this.createFirstIncome = true;
    }

    public void setSaveFivePercentOnLastWeek(){
        this.saveFivePercentOnLastWeek = true;
    }

    public void setSaveTenPercentOnLastWeek(){
        this.saveTenPercentOnLastWeek = true;
    }

    public int plusConsecutiveNoSpend(){
        return ++this.consecutiveNoSpend;
    }

    public int plusCreateBudget(){
        return ++this.createBudget;
    }

    public void setSuccessBudgetPlanLastMonth(boolean successBudgetPlanLastMonth) { this.successBudgetPlanLastMonth=successBudgetPlanLastMonth; }

    public int plusSuccessBudgetPlan(){
        return ++this.successBudgetPlan;
    }

    public void resetSuccessBudgetPlan(){
        this.successBudgetPlan=0;
    }

    public int plusGetWarningBudget(){
        return ++this.getWarningBudget;
    }

    public void setSaveFivePercentFromBudget(){
        this.saveFivePercentFromBudget = true;
    }

    public void setSaveTenPercentFromBudget(){
        this.saveTenPercentFromBudget = true;
    }

    public int plusInviteFriendToService(){
        return ++this.inviteFriendToService;
    }

    public int plusInviteFriendToChallenge(){
        return ++this.inviteFriendToChallenge;
    }

    public int plusShareTips(){
        return ++this.shareTips;
    }


}
