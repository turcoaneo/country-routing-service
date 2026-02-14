package com.ovidiu.countryrouting.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class TimingAspect {

    private final Path output = Paths.get("dfs-benchmark.csv");

    public TimingAspect() {
        try {
            if (!Files.exists(output)) {
                Files.writeString(output, "method,args,durationMs\n",
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            log.error("Failed to initialize benchmark CSV file", e);
        }
    }

    @Around("@annotation(com.ovidiu.countryrouting.aop.TrackExecutionTime)")
    public Object measure(ProceedingJoinPoint pjp) throws Throwable {

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method methodObj = signature.getMethod();
        TrackExecutionTime annotation = methodObj.getAnnotation(TrackExecutionTime.class);

        long start = System.nanoTime();
        Object result = pjp.proceed();
        long end = System.nanoTime();

        long durationMs = (end - start) / 1_000_000;

        String method = signature.toShortString();
        String args = annotation.includeArgs()
                ? Arrays.toString(pjp.getArgs())
                : "";

        log.info("Execution of {} took {} ms", method, durationMs);

        // Write to CSV
        try {
            String line = method + "," + args + "," + durationMs + "\n";
            Files.writeString(output, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("Failed to write benchmark entry", e);
        }

        return result;
    }
}
