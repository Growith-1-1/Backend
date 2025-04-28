package dev.book.user.service;

import dev.book.achievement.achievement_user.service.IndividualAchievementStatusService;
import dev.book.achievement.achievement_user.entity.AchievementUser;
import dev.book.achievement.achievement_user.repository.AchievementUserRepository;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.global.config.security.jwt.JwtUtil;
import dev.book.global.config.security.service.refresh.RefreshTokenService;
import dev.book.global.entity.Category;
import dev.book.global.exception.category.CategoryErrorCode;
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
import dev.book.user.user_category.UserCategory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AchievementUserRepository achievementUserRepository;

    private final RefreshTokenService refreshTokenService;
    private final IndividualAchievementStatusService individualAchievementStatusService;
    private final JwtUtil jwtUtil;

    public UserProfileResponse getUserProfile(CustomUserDetails userDetails) {
        return UserProfileResponse.fromEntity(userDetails.user());
    }

    @Transactional
    public UserProfileResponse updateUserProfile(UserProfileUpdateRequest profileUpdateRequest, CustomUserDetails userDetails) {

        validateNickname(profileUpdateRequest.nickname());

        UserEntity user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));
        user.updateNickname(profileUpdateRequest.nickname());
        user.updateProfileImage(profileUpdateRequest.profileImageUrl());
        return UserProfileResponse.fromEntity(user);
    }

    public void validateNickname(String nickname) {
        boolean isExisted = userRepository.existsByNickname(nickname);
        if (isExisted) throw new UserErrorException(UserErrorCode.DUPLICATE_NICKNAME);
    }

    /**
     * Cookie에 설정된 AcessToken, RefreshToken 삭제
     * DB에서 유저 완전히 삭제
     * @param request
     * @param response
     * @param userDetails
     */
    @Transactional
    public void deleteUser(HttpServletRequest request, HttpServletResponse response, CustomUserDetails userDetails) {
        UserEntity user = userDetails.user();
        refreshTokenService.deleteRefreshToken(user);
        individualAchievementStatusService.deleteIndividualAchievementStatus(user);
        userRepository.delete(user);
        jwtUtil.deleteAccessTokenAndRefreshToken(request, response);
    }

    /**
     * 유저의 카테고리 변경
     * @param userCategoriesRequest
     * @param userDetails
     */
    @Transactional
    public void updateUserCategories(UserCategoriesRequest userCategoriesRequest, CustomUserDetails userDetails) {
        UserEntity user = userRepository.findByEmail(userDetails.getUsername())
                        .orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));
        checkCategoriesAndUpdate(userCategoriesRequest.categories(), user);
    }

    public void checkCategoriesAndUpdate(List<String> categories, UserEntity user) {
        List<Category> categoryList = categoryRepository.findByCategoryIn(categories);
        if (!categories.isEmpty() && categoryList.size() != categories.size())
            throw new CategoryException(CategoryErrorCode.CATEGORY_BAD_REQUEST);
        user.updateCategory(categoryList);
    }

    /**
     * 유저의 로그인 상태를 true로 변경
     * @param userDetails
     */
    public void checkIsUserLogin(CustomUserDetails userDetails) {
        individualAchievementStatusService.deterMineContinuousLogin(userDetails.user());
    }

    /**
     * nickname 중복 체크 (중복이라면 409에러 반환)
     * @param nickname
     */
    public void checkIsValidateNickname(String nickname) {
        validateNickname(nickname);
    }

    /**
     * 유저의 닉네임 삭제
     * @param userDetails
     */
    @Transactional
    public void deleteUserNickname(CustomUserDetails userDetails) {
        UserEntity user = userRepository.findByEmail(userDetails.getUsername())
                        .orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));
        user.deleteNickname();
    }

    /**
     * 유저의 카테고리 반환
     * @param userDetails
     * @return
     */
    public UserCategoryResponse getUserCategories(CustomUserDetails userDetails) {
        UserEntity user = userRepository.findByEmailWithCategories(userDetails.getUsername())
                .orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));
        List<UserCategory> categories = user.getUserCategory();
        List<String> userCategories = new ArrayList<>();
        for (UserCategory category : categories){
            userCategories.add(category.getCategory().getKorean());
        }
        return new UserCategoryResponse(userCategories);
    }

    public List<UserAchievementResponse> getUserAchievement(CustomUserDetails userDetails) {
        List<AchievementUser> achievementUsers = achievementUserRepository.findAllByUser(userDetails.user());
        return achievementUsers.stream().map(
                achievementUser -> new UserAchievementResponse(
                                achievementUser.getAchievement().getTitle(),
                                achievementUser.getAchievement().getContent(),
                                achievementUser.getCreatedAt()
                )).toList();
    }

    public UserChallengeInfoResponse getUserChallengeInfo(CustomUserDetails userDetails) {
        UserEntity user = userDetails.user();
        return new UserChallengeInfoResponse(user.getSavings(), user.getCompletedChallenges(),
                user.getParticipatingChallenges(), user.getFinishedChallenge());
    }
}
