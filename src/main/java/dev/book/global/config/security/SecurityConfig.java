package dev.book.global.config.security;

import dev.book.global.config.security.dto.PermitUrlProperties;
import dev.book.global.config.security.filter.CheckFriendInviteFilter;
import dev.book.global.config.security.filter.JwtAuthenticationFilter;
import dev.book.global.config.security.handler.CustomAccessDeniedHandler;
import dev.book.global.config.security.handler.CustomAuthenticationEntryPoint;
import dev.book.global.config.security.handler.OAuth2FailureHandler;
import dev.book.global.config.security.handler.OAuth2SuccessHandler;
import dev.book.global.config.security.jwt.JwtUtil;
import dev.book.global.config.security.service.oauth2.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.List;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${springdoc.servers.production.url}")
    private String DOMAIN;

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final PermitUrlProperties permitUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(CsrfConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers(permitUrl.getUrl().toArray(new String[0])).permitAll()
                                .anyRequest().authenticated()
                        //todo 현재는 모든 경로를 열어놓음 추후 endpoint마다 권한 설정
                )
                .oauth2Login(
                        oauth2 -> oauth2
                                .authorizationEndpoint(authorization ->
                                        authorization.authorizationRequestRepository(OAuth2AuthorizationRequestBasedOnCookieRepository()))
                                .successHandler(oAuth2SuccessHandler)
                                .failureHandler(oAuth2FailureHandler)
                                .userInfoEndpoint(userInfo -> userInfo
                                        .userService(customOAuth2UserService))
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService, permitUrl), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new CheckFriendInviteFilter(), JwtAuthenticationFilter.class)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint(exceptionHandlerExceptionResolver()))
                        .accessDeniedHandler(new CustomAccessDeniedHandler(exceptionHandlerExceptionResolver())));
        return http.build();
    }


    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository OAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    @Bean
    public ExceptionHandlerExceptionResolver exceptionHandlerExceptionResolver() {
        return new ExceptionHandlerExceptionResolver();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080", "http://localhost:63342", "https://the-ham-phi.vercel.app", DOMAIN));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Cache-Control"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
