package dev.book.user.user_friend.controller.swagger;

import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.user.user_friend.dto.response.FriendListResponseDto;
import dev.book.user.user_friend.dto.response.FriendRequestListResponseDto;
import dev.book.user.user_friend.dto.response.InvitingUserTokenResponseDto;
import dev.book.user.user_friend.dto.response.KakaoResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "친구 API", description = "친구 초대 토큰 발급, 친구 조회, 친구 요청 관리, 삭제 api")
public interface UserFriendApi {

    @Operation(summary = "친구 초대 토큰 발급", description = "`카카오톡 공유하기` 전 친구 초대 토큰을 발급 후 해당 토큰을 포함한 `/api/v1/friends/invite?token={token}` 링크를 포함하여 `공유하기`를 보냅니다." +
                                                                    "\n\n 또한, serverCallBackArgs에 `invitingUserToken` : {token} 형식으로 넣어주시면 됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "친구 초대 토큰 발급 완료",
                    content = @Content(schema = @Schema(implementation = InvitingUserTokenResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "토큰 암호화에 실패하였습니다"),
            @ApiResponse(responseCode = "404", description = "유저를 찾을 수 없습니다.")
    })
    ResponseEntity<InvitingUserTokenResponseDto> getInviteUserToken(@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception;

    @Operation(summary= "카카오톡 웹훅용 (클라이언트 쪽 사용 X)", description = "카카오톡 웹 훅을 통해 `공유하기`가 성공했다는 것을 전달받는 api")
    ResponseEntity<?> getWebHookFromKakao(@RequestBody KakaoResponseDto kakaoResponseDto) throws Exception;

    @Operation(summary = "`카카오톡 공유하기` 링크 (클라이언트 쪽 사용 X) ", description = "`카카오톡 공유하기` 통해 접근하는 URL, 친구 초대 토큰을 통해 친구 요청을 생성한다."
                                                                        + "\n 해당 링크에 접속하게 되면 /main 화면으로 리다이렉트 됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "친구 요청 생성 완료"),
            @ApiResponse(responseCode = "404", description = "초대 요청을 한 내역을 찾을 수 없습니다."),
            @ApiResponse(responseCode = "404", description = "초대 요청을 받은 유저를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "500", description = "토큰 복호화에 실패하였습니다"),
    })
    ResponseEntity<?> getTokenAndMakeInvitation(HttpServletResponse response, @AuthenticationPrincipal CustomUserDetails userDetails,
                                                       @RequestParam(name="token") String token) throws Exception;


    @Operation(summary = "친구 목록 반환", description = "친구 목록을 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "친구 목록 반환 완료",
             content = @Content(
                     array = @ArraySchema(schema = @Schema(implementation = FriendListResponseDto.class))))
    })
    ResponseEntity<List<FriendListResponseDto>> getFriendList(@AuthenticationPrincipal CustomUserDetails userDetails);


    @Operation(summary = "친구 요청 목록 반환", description = "받은 친구 요청 목록을 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "친구 요청 목록 반환 완료",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = FriendRequestListResponseDto.class))))
    })
    ResponseEntity<List<FriendRequestListResponseDto>> getFriendRequestList(@AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "친구 요청 승낙", description = "친구 요청을 승낙합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "친구 요청 승낙 완료",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = FriendRequestListResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "친구 유저를 찾을 수 없습니다"),
            @ApiResponse(responseCode = "404", description = "친구 요청을 찾을 수 없습니다.")
    })
    ResponseEntity<?> acceptFriend(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("friend_id") Long friendId);

    @Operation(summary = "친구 요청 거절", description = "친구 요청을 거절합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "친구 요청 거절 완료"),
            @ApiResponse(responseCode = "404", description = "친구 유저를 찾을 수 없습니다"),
            @ApiResponse(responseCode = "404", description = "친구 요청을 찾을 수 없습니다.")
    })
    ResponseEntity<?> rejectFriend(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("friend_id") Long friendId);


    @Operation(summary = "친구 삭제", description = "친구를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "친구 삭제 완료",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = FriendRequestListResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "친구 유저를 찾을 수 없습니다")
    })
    ResponseEntity<?> deleteFriend(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("friend_id") Long friendId);

    }
