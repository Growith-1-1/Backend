package dev.book.global.config.security.service;

import dev.book.global.config.security.dto.CustomOAuth2User;
import dev.book.global.config.security.dto.OAuth2Attributes;
import dev.book.global.config.security.exception.CustomOAuth2Error;
import dev.book.global.config.security.exception.UnValidatedProviderException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * OAuth2 로그인 결과를 CustomOAuth2User로 생성하여 SecurityContext에 등록한다.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String provider = userRequest.getClientRegistration().getRegistrationId();

        if (provider.equals("kakao")){
            OAuth2Attributes kakaoAttributes = OAuth2Attributes.toKakao(attributes);
            return new CustomOAuth2User(kakaoAttributes.email(), attributes);
        }
        CustomOAuth2Error oAuth2Error = new CustomOAuth2Error("Invalid_Provider", "지원되지 않는 공급자입니다.", null, 403);
        throw new UnValidatedProviderException(oAuth2Error);
    }

}
