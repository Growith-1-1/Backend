package dev.book.user.controller;

import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.user.controller.swagger.UserApi;
import dev.book.user.dto.request.UserCategoriesRequest;
import dev.book.user.dto.request.UserProfileUpdateRequest;
import dev.book.user.dto.response.UserAchievementResponse;
import dev.book.user.dto.response.UserCategoryResponse;
import dev.book.user.dto.response.UserChallengeInfoResponse;
import dev.book.user.dto.response.UserProfileResponse;
import dev.book.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UserApi {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok()
                .body(userService.getUserProfile(userDetails));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateUserProfile(@RequestBody UserProfileUpdateRequest profileUpdateRequest,
                                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok()
                .body(userService.updateUserProfile(profileUpdateRequest, userDetails));
    }

    @GetMapping("/categories")
    public ResponseEntity<UserCategoryResponse> getUserCategories(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok()
                .body(userService.getUserCategories(userDetails));
    }

    @PutMapping("/categories")
    public ResponseEntity<?> updateUserCategories(@RequestBody UserCategoriesRequest userCategoriesRequest,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.updateUserCategories(userCategoriesRequest, userDetails);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser(HttpServletRequest request, HttpServletResponse response,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteUser(request, response, userDetails);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check/login") //로그인 확인
    public ResponseEntity<?> checkIsUserLogin(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.checkIsUserLogin(userDetails);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate/nickname")
    public ResponseEntity<Boolean> checkIsValidateNickname(@RequestParam(name = "nickname", required = true) String nickname) {
        userService.checkIsValidateNickname(nickname);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/nickname")
    public ResponseEntity<?> deleteUserNickname(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteUserNickname(userDetails);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/achievement")
    public ResponseEntity<List<UserAchievementResponse>> getUserAchievement(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok()
                .body(userService.getUserAchievement(userDetails));
    }

    @GetMapping("/challenge")
    public ResponseEntity<UserChallengeInfoResponse> getUserChallengeInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok()
                .body(userService.getUserChallengeInfo(userDetails));
    }

    @GetMapping("/test")
    public void loginTestUser(HttpServletResponse response) throws IOException {
        userService.login(response);
    }
}
