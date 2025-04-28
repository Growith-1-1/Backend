package dev.book.achievement.service;

import dev.book.achievement.achievement_user.entity.AchievementUser;
import dev.book.achievement.achievement_user.repository.AchievementUserRepository;
import dev.book.achievement.dto.AchievementResponseDto;
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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final FcmTokenRepository fcmTokenRepository;
    private final AchievementUserRepository achievementUserRepository;
    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final FCMService fcmService;
    private final SseService sseService;

    /**
     * 업적 저장 후 fcm 알림 발송 & 웹소켓을 통한 실시간 유저 알림
     * @param achievementId
     * @param userId
     */
    public void saveAchievement(Long achievementId, Long userId){
        if (!achievementUserRepository.existsByAchievementIdAndUserId(achievementId, userId)){ //업적이 존재하지 않으면 새로 등록
            Achievement achievement = achievementRepository.findById(achievementId)
                    .orElseThrow(() -> new AchievementException(AchievementErrorCode.ACHIEVEMENT_BAD_REQUEST));
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));
            AchievementUser achievementUser = new AchievementUser(user, achievement);
            achievementUserRepository.save(achievementUser);
            eventPublisher.publishEvent(new GetAchievementEvent(achievement, userId));
        }
    }


    /**
     * 달성한 업적에 대하여 FCM 알림을 보냅니다.
     * @param event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFcmAchievementNotification(GetAchievementEvent event){
        FcmToken token = fcmTokenRepository.findByUserId(event.userId())
                .orElseThrow(() -> new FcmTokenErrorException(FcmTokenErrorCode.NOT_FOUND_FCM_TOKEN));
        fcmService.sendAchievementNotification(token.getToken(), event.achievement());
    }

    /**
     * 달성한 업적에 대하여 유저에게 알려 팝업을 띄웁니다.
     * @param event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSseAchievementNotification(GetAchievementEvent event){
        AchievementResponseDto achievementResponseDto = AchievementResponseDto.from(event.achievement());
        sseService.sendAchievementToUser(event.userId(), achievementResponseDto);
    }


}
