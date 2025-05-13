package dev.book.global.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SseEmitterLogAspect {

    private final Map<String, SseEmitter> emitters;
    private final Map<String, Object> eventCache;
    private final ObjectMapper objectMapper;

    @Around("execution(* dev.book.global.sse.repository.SseEmitterRepositoryImpl.*(..))")
    public Object logSseEmitterMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();

        log.info("{}", objectMapper.writeValueAsString(Map.of(
                "layer", "sse",
                "action", "invoke",
                "method", method,
                "emitterCount", emitters.size(),
                "eventCacheCount", eventCache.size()
        )));

        Object result = joinPoint.proceed();

        log.info("{}", objectMapper.writeValueAsString(Map.of(
                "layer", "sse",
                "action", "return",
                "method", method,
                "result", formatResult(result),
                "emitterCount", emitters.size(),
                "eventCacheCount", eventCache.size()
        )));

        return result;
    }

    private Object formatResult(Object result) {
        if (result instanceof SseEmitter) {

            return "SseEmitter";
        }

        if (result instanceof Map) {

            return "Map(size=" + ((Map<?, ?>) result).size() + ")";
        }

        return result != null ? result.toString() : null;
    }
}
