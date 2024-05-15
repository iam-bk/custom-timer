package com.zepic.micrometer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * BK - <bk@zepic.com>
 */
@EnableAspectJAutoProxy
@Aspect
@RequiredArgsConstructor
@Component
@Slf4j
public class TimerAspect {

    private final MeterRegistry meterRegistry;

    @Pointcut(value = "@annotation(com.zepic.micrometer.TrackTime)")
    private void timerPointcut() {

    }

    @Around("timerPointcut()")
    public Object measureTime(ProceedingJoinPoint pjp) throws Throwable {
        Signature signature = pjp.getSignature();
        if(signature instanceof MethodSignature methodSignature) {
            String timerName = getTimerName(methodSignature);
            Object returnValue;
            Timer.Sample timed = Timer.start(meterRegistry);

            try {
                log.info("Timer: {}", timerName);
                returnValue = pjp.proceed();
            } catch (Throwable e) {
                this.record(timed, timerName, e);
                throw e;
            }

            this.record(timed, timerName, null);
            return returnValue;
        }

        return null;
    }

    private void record(Timer.Sample timed, String timerName, Throwable e) {
        timed.stop(Timer.builder(timerName).tags(new String[]{"exception", getExceptionClass(e)}).publishPercentileHistogram(true).publishPercentiles(0.95).register(meterRegistry));
    }

    private String getExceptionClass(Throwable e) {
        if(e == null) {
            return "none";
        }
        return e.getClass().getSimpleName();
    }

    private String getTimerName(MethodSignature methodSignature) {
        Method method = methodSignature.getMethod();
        TrackTime trackTime = method.getAnnotation(TrackTime.class);
        String timerName = trackTime.value();
        if(timerName != null && !timerName.isBlank()) {
            return timerName;
        } else {
            String methodName = method.getName();
            String className = method.getDeclaringClass().getSimpleName();
            return className + "." + methodName;
        }
    }

}
