package dev.book.user.service;

import dev.book.achievement.achievement_user.entity.AchievementUser;
import dev.book.achievement.achievement_user.repository.AchievementUserRepository;
import dev.book.achievement.achievement_user.service.IndividualAchievementStatusService;
import dev.book.achievement.entity.Achievement;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.global.config.security.jwt.JwtAuthenticationToken;
import dev.book.global.config.security.jwt.JwtUtil;
import dev.book.global.config.security.service.refresh.RefreshTokenService;
import dev.book.global.entity.Category;
import dev.book.global.exception.category.CategoryException;
import dev.book.global.repository.CategoryRepository;
import dev.book.user.dto.request.UserCategoriesRequest;
import dev.book.user.dto.request.UserProfileUpdateRequest;
import dev.book.user.dto.response.UserAchievementResponse;
import dev.book.user.dto.response.UserCategoryResponse;
import dev.book.user.dto.response.UserChallengeInfoResponse;
import dev.book.user.dto.response.UserProfileResponse;
import dev.book.user.entity.UserEntity;
import dev.book.user.exception.UserErrorCode;
import dev.book.user.exception.UserErrorException;
import dev.book.user.repository.UserRepository;
import dev.book.util.UserBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private IndividualAchievementStatusService individualAchievementStatusService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AchievementUserRepository achievementUserRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private JwtUtil jwtUtil;

    CustomUserDetails userDetails;

    @BeforeEach
    public void createUser(){
        UserEntity user = UserBuilder.of();
        userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(userDetails, userDetails.getAuthorities()));
    }


    @Test
    @DisplayName("유저의 프로필을 가져온다.")
    void getUserProfile() {
        //give
        UserEntity user = userDetails.user();
        // when
        UserProfileResponse userProfileResponse = userService.getUserProfile(userDetails);

        //then
        assertThat(userProfileResponse).isNotNull();
        assertThat(userProfileResponse.email()).isEqualTo(user.getEmail());
        assertThat(userProfileResponse.nickname()).isEqualTo(user.getNickname());
        assertThat(userProfileResponse.profileImageUrl()).isEqualTo(user.getProfileImageUrl());
    }

    @Test
    @DisplayName("유저의 프로필을 업데이트한다.")
    void updateUserProfile() {
        //given
        UserEntity user = userDetails.user();
        String changeNickname = "change";
        String changeProfile = "newProfile";
        UserProfileUpdateRequest userProfileUpdateRequest = new UserProfileUpdateRequest(changeNickname, changeProfile);
        given(userRepository.save(any(UserEntity.class))).willReturn(userDetails.user());

        //when
        UserProfileResponse userProfileResponse = userService.updateUserProfile(userProfileUpdateRequest, userDetails);

        //then
        assertThat(userProfileResponse).isNotNull();
        assertThat(userProfileResponse.email()).isEqualTo(user.getEmail());
        assertThat(userProfileResponse.nickname()).isEqualTo(changeNickname);
        assertThat(userProfileResponse.profileImageUrl()).isEqualTo(changeProfile);
    }

    @Test
    @DisplayName("닉네임이 중복된다면 에러가 발생한다.")
    void failUpdateUserProfile(){
        // given
        String duplicateNickname = "중복닉네임";
        String changeProfile = "newProfile";
        UserProfileUpdateRequest userProfileUpdateRequest = new UserProfileUpdateRequest(duplicateNickname, changeProfile);

        doThrow(new UserErrorException(UserErrorCode.DUPLICATE_NICKNAME))
                .when(userService).validateNickname(duplicateNickname);

        // when & then
        assertThatThrownBy(() -> userService.updateUserProfile(userProfileUpdateRequest, userDetails))
                .isInstanceOf(UserErrorException.class)
                .hasMessageContaining(UserErrorCode.DUPLICATE_NICKNAME.getMessage());

        verify(userService).validateNickname(duplicateNickname);
        verify(userRepository, never()).save(any());

    }


    @Test
    @DisplayName("유저를 삭제한다.")
    void deleteUser() {
        //when
        userService.deleteUser(request, response, userDetails);

        //then
        verify(jwtUtil).deleteAccessTokenAndRefreshToken(request, response);
        verify(userRepository).delete(userDetails.user());
        verify(refreshTokenService).deleteRefreshToken(userDetails.user());
        verify(individualAchievementStatusService).deleteIndividualAchievementStatus(userDetails.user());
    }

    @Test
    @DisplayName("유저 정보가 없다면 에러가 발생한다.")
    void failDeleteUser(){

        //when, then
        assertThatThrownBy(() -> userService.deleteUser(request, response, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("유저의 카테고리를 변경한다.")
    void changeCategories(){
        //given
        given(userRepository.findByEmail(any())).willReturn(Optional.ofNullable(userDetails.user()));
        List<String> categories = List.of("food", "cafe_snack");
        List<Category> categoryList = List.of(new Category("food", "음식"), new Category("cafe_snack", "카페 / 간식") );
        given(categoryRepository.findByCategoryIn(categories)).willReturn(categoryList);
        UserCategoriesRequest categoriesRequest = new UserCategoriesRequest(categories);

        //when
        userService.updateUserCategories(categoriesRequest, userDetails);

        //then
        assertThat(userDetails.user().getUserCategory().size()).isEqualTo(2);

    }

    @Test
    @DisplayName("카테고리 내역이 없다면 에러를 반환한다.")
    void checkCategories(){
        //given
        given(userRepository.findByEmail(any())).willReturn(Optional.ofNullable(userDetails.user()));
        List<String> categories = List.of("food", "cafe", "snack");
        List<Category> categoryList = List.of(new Category("food", "음식"));
        given(categoryRepository.findByCategoryIn(categories)).willReturn(categoryList);
        UserCategoriesRequest categoriesRequest = new UserCategoriesRequest(categories);

        //when & then
        assertThatThrownBy(() -> userService.updateUserCategories(categoriesRequest, userDetails))
                .isInstanceOf(CategoryException.class)
                        .hasMessageContaining("일치하지 않는 카테고리가 존재합니다.");

    }

    @Test
    @DisplayName("유저의 카테고리를 반환한다.")
    void getUserCategories(){
        //given
        UserEntity user = UserBuilder.withCategory(); // food, cafe_snack 으로 초기화
        userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(userDetails, userDetails.getAuthorities()));

        given(userRepository.findByEmailWithCategories(any())).willReturn(Optional.ofNullable(userDetails.user()));

        //when
        UserCategoryResponse userCategoryResponse = userService.getUserCategories(userDetails);

        //then
        assertThat(userCategoryResponse.categories().size()).isEqualTo(2);
        assertThat(userCategoryResponse.categories().contains("음식")).isTrue();
        assertThat(userCategoryResponse.categories().contains("카페 / 간식")).isTrue();
    }

    @Test
    @DisplayName("유저의 업적들을 반환한다.")
    void getUserAchievement(){
        //given
        UserEntity user = userDetails.user();
        Achievement achievement = new Achievement("업적", "내용");
        AchievementUser achievementUser = new AchievementUser(user, achievement);
        given(achievementUserRepository.findAllByUser(user)).willReturn(List.of(achievementUser));

        //when
        List<UserAchievementResponse> userAchievementResponses = userService.getUserAchievement(userDetails);

        //then
        assertThat(userAchievementResponses.size()).isEqualTo(1);
        assertThat(userAchievementResponses.get(0).title()).isEqualTo(achievement.getTitle());
        assertThat(userAchievementResponses.get(0).content()).isEqualTo(achievement.getContent());
    }

    @Test
    @DisplayName("유저의 챌린지 내용을 반환한다.")
    void getUserChallenge(){
        //given
        UserEntity user = userDetails.user();
        user.plusSavings(10000L);
        user.plusFinishedChallenge();
        user.plusCompleteChallenge();
        user.plusParticipatingChallenge();

        //when
        UserChallengeInfoResponse userChallengeInfoResponse = userService.getUserChallengeInfo(userDetails);

        //then
        assertThat(userChallengeInfoResponse.savings()).isEqualTo(user.getSavings());
        assertThat(userChallengeInfoResponse.finishedChallenge()).isEqualTo(user.getFinishedChallenge());
        assertThat(userChallengeInfoResponse.completedChallenges()).isEqualTo(user.getCompletedChallenges());
        assertThat(userChallengeInfoResponse.participatingChallenges()).isEqualTo(user.getParticipatingChallenges());

    }



}