package dev.book.user.entity;

import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.entity.Budget;
import dev.book.achievement.achievement_user.entity.AchievementUser;
import dev.book.challenge.challenge_invite.entity.ChallengeInvite;
import dev.book.challenge.entity.Challenge;
import dev.book.challenge.user_challenge.entity.UserChallenge;
import dev.book.global.config.security.dto.oauth2.OAuth2Attributes;
import dev.book.global.entity.BaseTimeEntity;
import dev.book.global.entity.Category;
import dev.book.tip.entity.Tip;
import dev.book.user.user_category.UserCategory;
import dev.book.user.user_friend.entity.UserFriend;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String email;

    @NotNull
    private String name;

    @NotNull
    private String nickname;

    private String profileImageUrl; //null인 경우 프로필 없음

    private long savings = 0;

    private int completedChallenges = 0;

    private int participatingChallenges = 0;

    private int finishedChallenge = 0;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserCategory> userCategory = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFriend> sendFriendRequests = new ArrayList<>(); //유저가 친구 요청 보냄

    @OneToMany(mappedBy = "friend", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFriend> receivedFriendRequests = new ArrayList<>(); //유저가 친구 요청 받음

    @OneToMany(mappedBy = "creator", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Challenge> challenges = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserChallenge> userChallenges = new ArrayList<>();

    @OneToMany(mappedBy = "requestUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeInvite> requestChallengeInvites = new ArrayList<>();

    @OneToMany(mappedBy = "inviteUser", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeInvite> receivedChallengeInvites = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AchievementUser> achievements = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountBook> accountBooks = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Budget> bugets = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tip> tips = new ArrayList<>();

    @Builder
    public UserEntity(String email, String name, String nickname, String profileImageUrl) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public static UserEntity of(OAuth2Attributes oAuth2Attributes) {
        return UserEntity.builder()
                .email(oAuth2Attributes.email())
                .name(oAuth2Attributes.nickname())
                .nickname("")
                .profileImageUrl(oAuth2Attributes.profileImageUrl())
                .build();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateCategory(List<Category> categories) {
        Set<Category> newCategorySet = new HashSet<>(categories);
        Set<Category> currentCategorySet = this.userCategory.stream()
                .map(UserCategory::getCategory)
                .collect(Collectors.toSet());

        //현재엔 있지만 새로운 리스트엔 없는 것
        Set<Category> toRemove = new HashSet<>(currentCategorySet);
        toRemove.removeAll(newCategorySet);

        //새로 들어왔지만 현재엔 없는 것
        Set<Category> toAdd = new HashSet<>(newCategorySet);
        toAdd.removeAll(currentCategorySet);

        // 삭제
        this.userCategory.removeIf(uc -> toRemove.contains(uc.getCategory()));

        // 추가
        for (Category category : toAdd) {
            UserCategory userCategory = new UserCategory(this, category);
            this.userCategory.add(userCategory);
        }
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void deleteNickname() { this.nickname = ""; }

    public void plusParticipatingChallenge() {
        this.participatingChallenges++;
    }

    public void minusParticipatingChallenge() {
        this.participatingChallenges--;
    }

    public void plusCompleteChallenge(){
        this.completedChallenges++;
    }

    public void plusSavings(long plusSavings){
        this.savings += plusSavings;
    }

    public void plusFinishedChallenge(){
        this.finishedChallenge++;
    }

}
