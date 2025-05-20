package dev.book.user.user_friend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.book.achievement.achievement_user.dto.event.InviteFriendToServiceEvent;
import dev.book.global.config.security.dto.CustomUserDetails;
import dev.book.user.entity.UserEntity;
import dev.book.user.exception.UserErrorCode;
import dev.book.user.exception.UserErrorException;
import dev.book.user.repository.UserRepository;
import dev.book.user.user_friend.dto.EncryptUserInfo;
import dev.book.user.user_friend.dto.response.FriendListResponseDto;
import dev.book.user.user_friend.dto.response.FriendRequestListResponseDto;
import dev.book.user.user_friend.dto.response.InvitingUserTokenResponseDto;
import dev.book.user.user_friend.dto.response.KakaoResponseDto;
import dev.book.user.user_friend.entity.UserFriend;
import dev.book.user.user_friend.exception.UserFriendErrorCode;
import dev.book.user.user_friend.exception.UserFriendException;
import dev.book.user.user_friend.repository.UserFriendRepository;
import dev.book.user.user_friend.util.AESUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserFriendService {

//    @Value("${springdoc.servers.production.url}")
    private String DOMAIN = "https://the-ham-phi.vercel.app";
    private final String MAIN_URL = "/main";

    private final UserRepository userRepository;
    private final UserFriendRepository userFriendRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    public InvitingUserTokenResponseDto getInviteUserToken(CustomUserDetails userDetails) throws Exception {
        Long id = userDetails.user().getId();
        String email = userDetails.getUsername();
        //초대를 보낸 유저의 정보로 암호화 토큰 생성
        EncryptUserInfo encryptUserInfo = new EncryptUserInfo(email, id, LocalDateTime.now());
        String token = AESUtil.encrypt(objectMapper.writeValueAsString(encryptUserInfo));
        String safeToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        return new InvitingUserTokenResponseDto(safeToken);
    }

    public void getWebHookFromKakao(KakaoResponseDto kakaoResponseDto) throws Exception {
        String requestUserToken = kakaoResponseDto.invitingUserToken();
        EncryptUserInfo userInfo = decryptToken(requestUserToken);
        //암호화된 토큰을 복호화하여 초대 토큰을 보낸 유저 저장
        UserEntity invitingUser = getUserEntity(userRepository.findById(userInfo.id()));

        hasExistingInvitation(userFriendRepository.existsByUserAndRequestedAt(invitingUser, userInfo.localDateTime()), UserFriendErrorCode.DUPLICATE_INVITATION);

        userFriendRepository.save(UserFriend.of(invitingUser, userInfo.localDateTime()));
    }

    //토큰이 포함된 url로 접근, 초대 토큰을 받은 상태를 처리
    @Transactional
    public void getTokenAndMakeInvitation(CustomUserDetails userDetails, HttpServletResponse response, String token) throws Exception {
        invitedUserMakeInvitation(userDetails.getUsername(), token);
        response.sendRedirect(DOMAIN+MAIN_URL);
    }

    public void invitedUserMakeInvitation(String email, String token) throws Exception {
        EncryptUserInfo userInfo = decryptToken(token);
        UserFriend userFriend = userFriendRepository.findByInvitingUserAndRequestedAt(userInfo.id(), userInfo.localDateTime())
                .orElseThrow(() -> new UserFriendException(UserFriendErrorCode.INVITING_NOT_FOUND));

        //내가 나 자신에게 친구 요청
        invitationMyself(userFriend.getUser(), email);
        //이미 초대 내역이 완성된 토큰
        hasExistingInvitation(userFriend.getFriend() != null, UserFriendErrorCode.ALREADY_MADE_INVITATION);

        UserEntity friend = getUserEntity(userRepository.findByEmail(email));
        userFriend.inviteFriend(friend);
        eventPublisher.publishEvent(new InviteFriendToServiceEvent(userFriend.getUser()));
    }

    private void invitationMyself(UserEntity user, String email) {
        if (Objects.equals(user.getEmail(), email))
            throw new UserFriendException(UserFriendErrorCode.MYSELF_INVITATION);
    }

    public List<FriendListResponseDto> getFriendList(CustomUserDetails userDetails) {
        UserEntity invitedUser = userDetails.user();
        //친구 요청이 승낙된 내역만
        List<UserEntity> friendLists = userFriendRepository.findAllByInvitedUserAndIsAcceptIsTrue(invitedUser.getId());
        return friendLists.stream()
                .map(FriendListResponseDto::of)
                .toList();
    }

    public List<FriendRequestListResponseDto> getFriendRequestList(CustomUserDetails userDetails) {
        UserEntity invitedUser = userDetails.user();
        //친구 요청이 요청 중인 내역만
        List<UserEntity> friendLists = userFriendRepository.findAllByInvitedUserAndIsRequestIsTrue(invitedUser.getId());
        return friendLists.stream()
                .map(FriendRequestListResponseDto::of)
                .toList();
    }

    @Transactional
    public void acceptFriend(CustomUserDetails userDetails, Long friendId) {
        UserFriend friendRequest = getRequestByUserAndFriend(userDetails.user(), friendId);
        friendRequest.accept();
        userFriendRepository.save(UserFriend.of(friendRequest.getUser(), friendRequest.getFriend()));
    }

    @Transactional
    public void rejectFriend(CustomUserDetails userDetails, Long friendId) {
        UserFriend friendRequest = getRequestByUserAndFriend(userDetails.user(), friendId);
        userFriendRepository.delete(friendRequest);
    }

    @Transactional
    public void deleteFriend(CustomUserDetails userDetails, Long friendId) {
        UserEntity friend = getUserEntity(friendId);
        //양방향 삭제
        userFriendRepository.deleteByUserAndFriendAndIsAcceptIsTrue(userDetails.user(), friend);
        userFriendRepository.deleteByUserAndFriendAndIsAcceptIsTrue(friend, userDetails.user());
    }

    private EncryptUserInfo decryptToken(String safeToken) throws Exception {
        String token = URLDecoder.decode(safeToken, StandardCharsets.UTF_8);
        String decryptUserInfo = AESUtil.decrypt(token);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper.readValue(decryptUserInfo, EncryptUserInfo.class);
    }

    private UserFriend getRequestByUserAndFriend(UserEntity user, Long friendId) {
        UserEntity requestFriendUser = getUserEntity(friendId);
        return userFriendRepository.findByUserAndFriendAndIsRequestIsTrue(requestFriendUser, user)
                .orElseThrow(() -> new UserFriendException(UserFriendErrorCode.FRIEND_REQUEST_NOT_FOUND));
    }

    private UserEntity getUserEntity(Long friendId) {
        return userRepository.findById(friendId)
                .orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));
    }

    private UserEntity getUserEntity(Optional<UserEntity> userRepository) {
        return userRepository
                .orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));
    }

    private void hasExistingInvitation(boolean userFriendRepository, UserFriendErrorCode duplicateInvitation) {
        if (userFriendRepository)
            throw new UserFriendException(duplicateInvitation);
    }
}
