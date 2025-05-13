package dev.book.user.controller.swagger;

import dev.book.accountbook.dto.response.AccountBookResponse;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.user.dto.request.UserCategoriesRequest;
import dev.book.user.dto.request.UserProfileUpdateRequest;
import dev.book.user.dto.response.UserAchievementResponse;
import dev.book.user.dto.response.UserCategoryResponse;
import dev.book.user.dto.response.UserChallengeInfoResponse;
import dev.book.user.dto.response.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "유저 API", description = "유저 프로필 조회, 수정, 삭제, 카테고리 관리, 마이페이지의 업적, 통계 반환 api")
public interface UserApi {

    @Operation(summary = "유저 삭제(회원 탈퇴)", description = "현재 로그인된 유저를 탈퇴시킵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 삭제 완료"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없습니다.")
    })
    ResponseEntity<?> deleteUser(HttpServletRequest request, HttpServletResponse response, @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "유저 프로필 반환", description = "현재 로그인된 유저의 프로필을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 프로필 정보 반환 완료",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없습니다.")
    })
    ResponseEntity<UserProfileResponse> getUserProfile(@AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "유저 프로필 수정", description = "현재 로그인된 유저의 프로필을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 프로필 수정 완료",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 닉네임입니다"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없습니다.")
    })
    ResponseEntity<UserProfileResponse> updateUserProfile(@RequestBody UserProfileUpdateRequest profileUpdateRequest,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "유저 카테고리 반환", description = "현재 로그인된 유저의 카테고리를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 카테고리 반환 완료"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없습니다.")
    })
    ResponseEntity<UserCategoryResponse> getUserCategories(@AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "유저 카테고리 수정", description = "현재 로그인된 유저의 카테고리를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 카테고리 수정 완료"),
            @ApiResponse(responseCode = "400", description = "일치하지 않는 카테고리가 존재합니다."),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없습니다.")
    })
    ResponseEntity<?> updateUserCategories(@RequestBody UserCategoriesRequest userCategoriesRequest,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "유저의 로그인 여부", description = "연속적인 로그인에 대한 업적을 확인하기 위해 유저가 로그인 했는지 확인합니다. 하루 최초 1번만 필수 호출합니다."
                                                            +"\n\n 만약 호출 시 연속적인 로그인에 대한 업적에 달성했을 경우, 달성했다는 알림이 갑니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저 로그인 여부 확인 완료"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없습니다.")
    })
    ResponseEntity<?> checkIsUserLogin(@AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "유저의 닉네임 중복 확인", description = "닉네임의 중복 여부를 체크합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "닉네임 설정 가능"),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 닉네임입니다")
    })
    ResponseEntity<Boolean> checkIsValidateNickname(@RequestParam(name = "nickname", required = true) String nickname);

    @Operation(summary = "유저의 닉네임 삭제 (실제 사용 X)", description = "현재 유저의 닉네임을 삭제합니다. \"\" 상태로 저장됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "닉네임 삭제 완료"),
    })
    ResponseEntity<?> deleteUserNickname(@AuthenticationPrincipal CustomUserDetails userDetails);


    @Operation(summary = "유저의 업적 내용 반환", description = "유저가 달성한 업적들의 내용들을 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저의 업적들 반환 완료",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AccountBookResponse.class)))),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없습니다.")
    })
    ResponseEntity<List<UserAchievementResponse>> getUserAchievement(@AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "유저의 챌린지 정보 반환", description = "유저가 챌린지로 절약한 금액, 성공한 챌린지 수, 참여 중인 챌린지 수, 성공한 챌린지 수를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유저의 챌린지 정보 반환 완료",
                    content = @Content(schema = @Schema(implementation = UserChallengeInfoResponse.class))),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없습니다.")
    })
    ResponseEntity<UserChallengeInfoResponse> getUserChallengeInfo(@AuthenticationPrincipal CustomUserDetails userDetails);
}

