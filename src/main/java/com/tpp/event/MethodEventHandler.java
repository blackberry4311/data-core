package com.tpp.event;

import java.lang.annotation.*;

/**
 * This annotation is to declare an event handler
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MethodEventHandler {

    /**
     * name of the even bus
     *
     * @return
     */
    String eventBus() default "generalEventBus";

    /**
     * name of the event to implement
     *
     * @return
     */
    String[] eventName() default {};
}
