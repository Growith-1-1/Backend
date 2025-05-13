package dev.book.global.config.Firebase.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import dev.book.accountbook.dto.event.CreateTransEvent;
import dev.book.achievement.entity.Achievement;
import dev.book.global.config.Firebase.dto.LimitWarningFcmEvent;
import dev.book.global.config.Firebase.entity.FcmToken;
import dev.book.global.config.Firebase.exception.FcmTokenErrorCode;
import dev.book.global.config.Firebase.exception.FcmTokenErrorException;
import dev.book.global.config.Firebase.repository.FcmTokenRepository;
import dev.book.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {
    private final ObjectMapper objectMapper;
    private final FcmTokenRepository fcmTokenRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLimitWarningFcmEvent(LimitWarningFcmEvent event) {
        FcmToken token = fcmTokenRepository.findByUserId(event.userId())
                .orElseThrow(() -> new FcmTokenErrorException(FcmTokenErrorCode.NOT_FOUND_FCM_TOKEN));

        sendSpendNotification(token.getToken(), event.nickname(), event.budget(), event.total(), event.usageRate(), event.userId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendGetTrans(CreateTransEvent event) {
        FcmToken token = fcmTokenRepository.findByUserId(event.user().getId())
                .orElseThrow(() -> new FcmTokenErrorException(FcmTokenErrorCode.NOT_FOUND_FCM_TOKEN));

        sendCreateTransEvent(token.getToken(), event.user());
    }

    public void sendAchievementNotification(String fcmToken, Achievement achievement) {
        Message message = messageBuild(fcmToken, achievement.getTitle(), achievement.getContent());

        try {
            String response = FirebaseMessaging.getInstance().send(message);

            log.info("{}", toJson(Map.of(
                    "layer", "fcm",
                    "type", "achievement",
                    "title", achievement.getTitle(),
                    "body", achievement.getContent(),
                    "token", fcmToken,
                    "status", "success",
                    "response", response
            )));
        } catch (FirebaseMessagingException e) {
            log.error("{}", toJson(Map.of(
                    "layer", "fcm",
                    "type", "achievement",
                    "title", achievement.getTitle(),
                    "body", achievement.getContent(),
                    "token", fcmToken,
                    "status", "failure",
                    "error", e.getMessage()
            )));
        }
    }

    public void saveToken(UserEntity user, String token) {
        fcmTokenRepository.save(new FcmToken(user, token));
    }

    private Message messageBuild(String fcmToken, String title, String body) {
        return Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();
    }

    public String getToken(Long userId) {
        FcmToken fcmToken = fcmTokenRepository.findById(userId).orElseThrow();

        return fcmToken.getToken();
    }

    private void sendSpendNotification(String fcmToken, String userName, int budget, long amount, long percent, Long userId) {
        String title = "지출 알림";
        String body = userName + "님, 현재까지 지출은 " + amount + "원입니다." +
                "정하신 예산" + budget + "원 에서" + percent + "% 만큼 사용하셨습니다.";

        Message message = messageBuild(fcmToken, title, body);

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("{}", toJson(Map.of(
                    "layer", "fcm",
                    "type", "limitWarning",
                    "userId", userId,
                    "title", title,
                    "body", body,
                    "token", fcmToken,
                    "status", "success",
                    "response", response
            )));
        } catch (FirebaseMessagingException e) {
            log.error("{}", toJson(Map.of(
                    "layer", "fcm",
                    "type", "limitWarning",
                    "userId", userId,
                    "title", title,
                    "body", body,
                    "token", fcmToken,
                    "status", "failure",
                    "error", e.getMessage()
            )));
        }
    }

    private void sendCreateTransEvent(String fcmToken, UserEntity user) {
        String title = "거래내역 동기화";
        String body = user.getName() + "님의 거래내역을 찾았어요!";

        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("destinationUrl", "transactionPage")
                .putData("userId", String.valueOf(user.getId()))
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("{}", toJson(Map.of(
                    "layer", "fcm",
                    "type", "transactionSync",
                    "userId", user.getId(),
                    "title", title,
                    "body", body,
                    "token", fcmToken,
                    "status", "success",
                    "response", response
            )));
        } catch (FirebaseMessagingException e) {
            log.error("{}", toJson(Map.of(
                    "layer", "fcm",
                    "type", "transactionSync",
                    "userId", user.getId(),
                    "title", title,
                    "body", body,
                    "token", fcmToken,
                    "status", "failure",
                    "error", e.getMessage()
            )));
        }
    }

    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{\"layer\":\"fcm\",\"error\":\"json conversion failed\"}";
        }
    }
}
