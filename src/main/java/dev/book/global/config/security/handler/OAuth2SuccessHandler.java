package dev.book.global.config.security.handler;

import dev.book.global.config.security.jwt.JwtUtil;
import dev.book.global.config.security.service.oauth2.OAuth2AuthService;
import dev.book.global.config.security.util.CookieUtil;
import dev.book.user.enums.UserLoginState;
import dev.book.user.user_friend.service.UserFriendService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String REQUEST_USER_TOKEN = "request_user_token";

    private final OAuth2AuthService oauth2AuthService;
    private final JwtUtil jwtUtil;
    private final UserFriendService userFriendService;

    @Transactional
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String invitationToken = extractInvitationToken(request, response);
        UserLoginState userLoginState = oauth2AuthService.getAttributes(authentication);

        // 친구 요청 처리
        handleFriendInvitation(authentication, invitationToken);

        switch (userLoginState){
            case LOGIN_SUCCESS -> {
                jwtUtil.generateToken(response, authentication);
                getRedirectStrategy().sendRedirect(request, response, "https://the-ham-phi.vercel.app/main");
            }
            case PROFILE_INCOMPLETE ->
                    getRedirectStrategy().sendRedirect(request, response, "https://the-ham-phi.vercel.app/auth/signup?email="+authentication.getName());
        }
    }

    private String extractInvitationToken(HttpServletRequest request, HttpServletResponse response) {
        String token = CookieUtil.getCookie(request, REQUEST_USER_TOKEN);
        CookieUtil.deleteCookie(request, response, REQUEST_USER_TOKEN);
        return token;
    }


    /**
     * 친구 초대 URL로 접근한 유저인 경우, 친구를 요청한 유저의 토큰이 존재
     * 해당 토큰을 통해 누구의 초대로 접속한 것인지 판단하여 친구 요청 생성
     * @param authentication
     * @param safeToken
     */
    private void handleFriendInvitation(Authentication authentication, String safeToken) {
        if (safeToken == null) return;

        try {
            String invitationToken = URLDecoder.decode(safeToken, StandardCharsets.UTF_8);
            userFriendService.invitedUserMakeInvitation(authentication.getName(), invitationToken);
        } catch (Exception e) {
            logger.error("친구 요청 처리 중 오류 발생", e);
        }
    }
}
