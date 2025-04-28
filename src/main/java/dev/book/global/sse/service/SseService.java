package dev.book.global.sse.service;

import dev.book.achievement.dto.AchievementResponseDto;
import dev.book.global.sse.dto.response.SseAchievementResponse;
import dev.book.global.sse.dto.response.SseTipResponse;
import dev.book.global.sse.repository.SseEmitterRepository;
import dev.book.global.sse.type.SseType;
import dev.book.tip.dto.response.TipResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SseService {

    private final SseEmitterRepository sseEmitterRepository;
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    /**
     * SSE 구독 경로, 구독한 emitter(유저)를 저장하고 Last Event Id보다 이후에 발생한 event들을 전송합니다.
     * @param userId
     * @param lastEventId
     * @return
     */
    public SseEmitter subscribe(Long userId, String lastEventId) {
        String emitterId = getEmitterId(userId);
        SseEmitter emitter = sseEmitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> sseEmitterRepository.deleteEmitterById(emitterId));
        emitter.onTimeout(() -> sseEmitterRepository.deleteEmitterById(emitterId));
        emitter.onError((e) -> sseEmitterRepository.deleteEmitterById(emitterId));

        sendToClient(emitter, emitterId, "EventStream created. [userId= "+ userId + "]", null);
        //구독 되었다는 것을 표시하는 eventCache

        if (!lastEventId.isEmpty()){
            Map<String, Object> events = sseEmitterRepository.findAllEventCacheStartsWithUserId(String.valueOf(userId));
            events.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey())<0)
                .forEach(entry -> {
                    SseAchievementResponse event = (SseAchievementResponse) entry.getValue();
                    sendToClient(emitter, entry.getKey(), event, event.name());
                });
            sseEmitterRepository.deleteAllEventCacheStartsWithUserId(String.valueOf(userId));
        }

        return emitter;
    }

    /**
     * emitter(userId) 에게 event(AchievementResponseDto) 를 전송합니다.
     * @param userId
     * @param achievement
     */
    public void sendAchievementToUser(Long userId, AchievementResponseDto achievement) {
        Map<String, SseEmitter> sseEmitters = sseEmitterRepository.findAllEmitterStartsWithUserId(String.valueOf(userId));
        String emitterId = getEmitterId(userId);
        SseAchievementResponse sseAchievementResponse = new SseAchievementResponse(emitterId, achievement.title(), achievement.content(), SseType.ACHIEVEMENT.name());
        if (sseEmitters.isEmpty()){ //emitter가 존재하지 않을 경우, 백그라운드에서 SSE 이벤트가 발생하였을 경우
            sseEmitterRepository.saveEventCache(emitterId, sseAchievementResponse); //event 캐시에만 저장해놓고 subscribe 되었을 때 반환하도록 한다.
        }
        else {
            sseEmitters.forEach((key, emitter) -> {
                sseEmitterRepository.saveEventCache(emitterId, sseAchievementResponse);
                sendToClient(emitter, emitterId, sseAchievementResponse, sseAchievementResponse.name());
            });
        }
    }

    /**
     * 전체 emitter에게 event(TipResponse)를 전송합니다.
     * @param tip
     */
    public void sendTipToAllUsers(TipResponse tip) {
        String eventId = "tip_" + System.currentTimeMillis();
        SseTipResponse sseTipResponse = new SseTipResponse(eventId, tip.writer(), tip.content(), SseType.TIP.name());

        Map<String, SseEmitter> emitters = sseEmitterRepository.findAllEmitters(); // 전체 유저에게 브로드캐스트
        emitters.forEach((emitterId, emitter)
                -> sendToClient(emitter, emitterId, sseTipResponse, SseType.TIP.name()));
    }

    private void sendToClient(SseEmitter emitter, String emitterId, Object value, String name) {
        try {
            emitter.send(SseEmitter.event()
                    .id(emitterId)
                    .name(name)
                    .data(value));
        } catch (IOException | IllegalStateException e){
            sseEmitterRepository.deleteEmitterById(emitterId);
        }
    }

    private static String getEmitterId(Long userId) {
        return userId + "_" + System.currentTimeMillis();
    }

}
