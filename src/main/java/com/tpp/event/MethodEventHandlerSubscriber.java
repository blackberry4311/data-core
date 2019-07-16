package com.tpp.event;

import com.tpp.EventBus;
import com.tpp.persistence.util.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This event handler discover all {@link MethodEventHandler} in an {@link ApplicationContext}
 * to subscribe them to target event bus.
 */
public class MethodEventHandlerSubscriber
        implements ApplicationContextAware, ApplicationListener<ApplicationContextEvent> {
    protected static final Logger logger = Logger.getLogger(MethodEventHandlerSubscriber.class);

    static final List<Object> subscribed = new ArrayList<>();

    ApplicationContext appContext;

    /**
     * subscribe method implementation of bean to the bus in application context
     *
     * @param methods
     * @param bean
     * @param applicationContext
     */
    private static void subscribeMethodHandlers(List<Method> methods, Object bean,
            ApplicationContext applicationContext) {
        Class<?> clazz = bean.getClass();
        for (Method method : methods) {
            MethodEventHandler handler = method.getAnnotation(MethodEventHandler.class);
            EventBus bus = applicationContext.getBean(handler.eventBus(), EventBus.class);
            for (String event : handler.eventName()) {
                bus.subscribe(event, MethodEventHandlerImpl.create(method, bean));
                if (logger.isDebugEnabled()) logger.debug(
                        "registered handler from " + clazz.getName() + "." + method.getName() + " for " + event
                                + " event in " + handler.eventBus());
            }
            logger.info(
                    "subscribed " + StringUtils.join(handler.eventName(), ", ") + " in " + handler.eventBus() + " to "
                            + clazz.getName() + "." + method.getName());
        }
    }

    /**
     * implements {@link ApplicationContextAware} to target only container application context
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        appContext = applicationContext;
        logger.info("waiting for application context to subscribe event " + appContext.getDisplayName());
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        if (event.getApplicationContext() != appContext) return;
        if (event instanceof ContextRefreshedEvent) {
            subcribeContextHandler(appContext);
        }
    }

    private void subcribeContextHandler(ApplicationContext applicationContext) {
        Map<String, Object> beans = applicationContext.getBeansOfType(Object.class, false, true);
        List<Object> newSubscribed = new ArrayList<Object>();

        Map<Class<?>, List<Method>> methodCache = new HashMap<Class<?>, List<Method>>();

        for (Object bean : beans.values()) {
            if (bean == null) {
                continue; //TODO i dont know why there is still case bean was null in this application context
            }
            if (subscribed.contains(bean)) {
                logger.warn("found subcribed bean in " + applicationContext.getDisplayName() + ": " + bean);
                continue;
            }
            List<Method> methods = methodCache.get(bean.getClass());
            if (methods == null) {
                methods = ClassUtils.getAnnotatedMethods(MethodEventHandler.class, bean.getClass());
                methodCache.put(bean.getClass(), methods);
            }
            if (methods.isEmpty()) {
                continue;
            }
            subscribeMethodHandlers(methods, bean, applicationContext);
            newSubscribed.add(bean);
        }

        logger.info("subcribed new " + newSubscribed.size() + " bean(s)");
        subscribed.addAll(newSubscribed);
    }
}
