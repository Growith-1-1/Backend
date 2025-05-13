package dev.book.global.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketLog {
    private final AtomicInteger connectedUsers = new AtomicInteger(0);
    private final ObjectMapper objectMapper;

    @EventListener
    public void onConnect(SessionConnectedEvent event) {
        int count = connectedUsers.incrementAndGet();

        log.info("{}", toJson(Map.of(
                "layer", "websocket",
                "event", "connected",
                "connectedUsers", count
        )));
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        int count = connectedUsers.decrementAndGet();

        log.info("{}", toJson(Map.of(
                "layer", "websocket",
                "event", "disconnected",
                "connectedUsers", count
        )));
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{\"layer\":\"websocket\",\"error\":\"json conversion failed\"}";
        }
    }
}
