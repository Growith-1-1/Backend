package dev.book.global.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ControllerLogAspect {
    private final ObjectMapper objectMapper;

    @Around("execution(* dev.book..controller..*(..))")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        String methodName = joinPoint.getSignature().toShortString();

        log.info("{}", objectMapper.writeValueAsString(Map.of(
                "layer", "controller",
                "method", methodName
        )));

        return result;
    }
}
