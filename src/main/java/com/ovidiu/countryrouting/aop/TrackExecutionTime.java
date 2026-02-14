package com.ovidiu.countryrouting.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackExecutionTime {
    boolean includeArgs() default false;
}
