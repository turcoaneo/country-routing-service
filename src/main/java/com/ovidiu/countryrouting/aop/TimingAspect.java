package com.ovidiu.countryrouting.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class TimingAspect {

    @Around("@annotation(com.ovidiu.countryrouting.aop.TrackExecutionTime)")
    public Object measure(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        Object result = pjp.proceed();
        long end = System.nanoTime();

        long durationMs = (end - start) / 1_000_000;

        log.info("Execution of {} took {} ms",
                pjp.getSignature().toShortString(),
                durationMs);

        return result;
    }
}