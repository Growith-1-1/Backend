package dev.book.achievement.service;

import dev.book.achievement.achievement_user.entity.AchievementUser;
import dev.book.achievement.achievement_user.repository.AchievementUserRepository;
import dev.book.achievement.dto.event.GetAchievementEvent;
import dev.book.achievement.entity.Achievement;
import dev.book.achievement.exception.AchievementErrorCode;
import dev.book.achievement.exception.AchievementException;
import dev.book.achievement.repository.AchievementRepository;
import dev.book.global.config.Firebase.entity.FcmToken;
import dev.book.global.config.Firebase.exception.FcmTokenErrorCode;
import dev.book.global.config.Firebase.exception.FcmTokenErrorException;
import dev.book.global.config.Firebase.repository.FcmTokenRepository;
import dev.book.global.config.Firebase.service.FCMService;
import dev.book.global.sse.service.SseService;
import dev.book.user.entity.UserEntity;
import dev.book.user.exception.UserErrorCode;
import dev.book.user.exception.UserErrorException;
import dev.book.user.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    FcmTokenRepository fcmTokenRepository;

    @Mock
    AchievementUserRepository achievementUserRepository;

    @Mock
    AchievementRepository achievementRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @Mock
    FCMService fcmService;

    @Mock
    SseService sseService;

    @InjectMocks
    AchievementService achievementService;

    @NotNull
    private static AchievementAndUser getAchievementAndUser() {
        Achievement achievement = mock(Achievement.class);
        ReflectionTestUtils.setField(achievement, "id", 1L);
        UserEntity user = mock(UserEntity.class);
        ReflectionTestUtils.setField(user, "id", 1L);
        return new AchievementAndUser(achievement, user);
    }

    private record AchievementAndUser(Achievement achievement, UserEntity user) {
    }

    @Test
    @DisplayName("업적을 저장한다.")
    void saveAchievement() {
        //given
        AchievementAndUser getAchievementAndUser = getAchievementAndUser();
        UserEntity user = getAchievementAndUser.user;
        Achievement achievement = getAchievementAndUser.achievement;

        given(achievementRepository.findById(anyLong())).willReturn(Optional.of(achievement));
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        given(achievementUserRepository.existsByAchievementIdAndUserId(anyLong(), anyLong())).willReturn(false);
        AchievementUser achievementUser = new AchievementUser(user, achievement);
        given(achievementUserRepository.save(any())).willReturn(achievementUser);

        //when
        achievementService.saveAchievement(achievement.getId(), user.getId());

        //then
        verify(achievementUserRepository).save(any());
        verify(applicationEventPublisher).publishEvent(new GetAchievementEvent(achievement, user.getId()));
    }

    @Test
    @DisplayName("업적에 달성한 적이 있다면 저장하지 않는다.")
    void exceptionAchievement() {
        //given
        AchievementAndUser getAchievementAndUser = getAchievementAndUser();
        given(achievementUserRepository.existsByAchievementIdAndUserId(anyLong(), anyLong())).willReturn(true);

        //when
        achievementService.saveAchievement(getAchievementAndUser.achievement().getId(), getAchievementAndUser.user().getId());

        //then
        verify(achievementUserRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(new GetAchievementEvent(getAchievementAndUser.achievement(), getAchievementAndUser.user().getId()));
    }

    @Test
    @DisplayName("유저를 찾을 수 없을 경우 에러가 발생한다.")
    void notFoundUser(){
        AchievementAndUser getAchievementAndUser = getAchievementAndUser();
        given(achievementRepository.findById(anyLong())).willReturn(Optional.of(getAchievementAndUser.achievement));
        given(userRepository.findById(getAchievementAndUser.user.getId())).willThrow(new UserErrorException(UserErrorCode.USER_NOT_FOUND));

        assertThatThrownBy(() -> achievementService.saveAchievement(getAchievementAndUser.achievement().getId(), getAchievementAndUser.user().getId()))
                .isInstanceOf(UserErrorException.class)
                .hasMessageContaining("유저를 찾을 수 없습니다.");
        verify(applicationEventPublisher, never()).publishEvent(new GetAchievementEvent(getAchievementAndUser.achievement(), getAchievementAndUser.user().getId()));
    }

    @Test
    @DisplayName("등록된 업적 _id가 아닐 경우 에러가 발생한다.")
    void notFoundAchievement(){
        AchievementAndUser getAchievementAndUser = getAchievementAndUser();
        given(achievementRepository.findById(getAchievementAndUser.achievement.getId())).willThrow(new AchievementException(AchievementErrorCode.ACHIEVEMENT_BAD_REQUEST));

        assertThatThrownBy(() -> achievementService.saveAchievement(getAchievementAndUser.achievement().getId(), getAchievementAndUser.user().getId()))
                .isInstanceOf(AchievementException.class)
                .hasMessageContaining("등록된 업적 _id가 아닙니다.");
        verify(applicationEventPublisher, never()).publishEvent(new GetAchievementEvent(getAchievementAndUser.achievement(), getAchievementAndUser.user().getId()));
    }

    @Test
    @DisplayName("달성한 업적에 대하여 FCM 알림을 보낸다.")
    void noFcmTokenException() {
        AchievementAndUser achievementAndUser = getAchievementAndUser();
        GetAchievementEvent event = new GetAchievementEvent(achievementAndUser.achievement, achievementAndUser.user.getId());
        given(fcmTokenRepository.findByUserId(anyLong())).willThrow(new FcmTokenErrorException(FcmTokenErrorCode.NOT_FOUND_FCM_TOKEN));

        assertThatThrownBy(() -> achievementService.handleFcmAchievementNotification(event))
                .isInstanceOf(FcmTokenErrorException.class)
                .hasMessageContaining("토큰을 찾을 수 없습니다.");

        verify(fcmService, never()).sendAchievementNotification(any(), any());
    }

    @Test
    @DisplayName("FCM 토큰이 없을 경우 에러가 발생한다.")
    void handleFcmAchievementNotification() {
        AchievementAndUser achievementAndUser = getAchievementAndUser();
        GetAchievementEvent event = new GetAchievementEvent(achievementAndUser.achievement, achievementAndUser.user.getId());
        FcmToken fcmToken = mock(FcmToken.class);
        given(fcmTokenRepository.findByUserId(anyLong())).willReturn(Optional.ofNullable(fcmToken));

        achievementService.handleFcmAchievementNotification(event);

        verify(fcmService).sendAchievementNotification(any(), any());
    }

    @Test
    @DisplayName("달성한 업적에 대하여 SSE 알림을 보낸다.")
    void handleSseAchievementNotification() {
        AchievementAndUser achievementAndUser = getAchievementAndUser();
        GetAchievementEvent event = new GetAchievementEvent(achievementAndUser.achievement, achievementAndUser.user.getId());

        achievementService.handleSseAchievementNotification(event);

        verify(sseService).sendAchievementToUser(any(), any());
    }
}