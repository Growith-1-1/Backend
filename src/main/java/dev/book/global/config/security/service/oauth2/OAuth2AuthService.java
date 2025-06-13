package dev.book.global.config.security.service.oauth2;

import dev.book.global.config.security.dto.oauth2.OAuth2Attributes;
import dev.book.global.config.security.exception.AuthErrorCode;
import dev.book.global.config.security.exception.AuthException;
import dev.book.user.entity.UserEntity;
import dev.book.user.enums.UserLoginState;
import dev.book.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * OAuth2User에서 attributes를 통해 유저의 상태 파악 후 UserLoginState 반환
 */
@Service
@RequiredArgsConstructor
public class OAuth2AuthService {

    private final UserRepository userRepository;

    public UserLoginState getAttributes(Authentication authentication){
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        if (provider.equals("kakao")){
            OAuth2Attributes kakaoAttributes = OAuth2Attributes.toKakao(attributes);
            return getUserLoginState(kakaoAttributes);
        }
        throw new AuthException(AuthErrorCode.UNVALIDATED_PROVIDER);
    }

    /**
     * DB에서 유저를 조회하여 존재한다면 LOGIN_SUCCESS
     * nickname이 비어 있거나 존재하지 않으면 현재 kakao 정보들만 저장한 후에 PROFILE_INCOMPLETE
     * @param OAuth2Attributes
     * @return
     */
    private UserLoginState getUserLoginState(OAuth2Attributes OAuth2Attributes) {
        UserEntity user = userRepository.findByEmail(OAuth2Attributes.email())
                .orElseGet(() -> UserEntity.of(OAuth2Attributes));

        if (user.getNickname().isBlank()){
            userRepository.save(user);
            return UserLoginState.PROFILE_INCOMPLETE;
        }
        return UserLoginState.LOGIN_SUCCESS;
    }

}
