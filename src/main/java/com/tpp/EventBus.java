package com.tpp;

import com.tpp.exception.EventException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * EventBus is a communication channel between event subscriber and consumer
 * ({@link EventHandler})
 *
 * <li>{@link #getHandlers(String, boolean)} to allow manipulation from subclass</li>
 * <li>{@link #logger} is protected and non-static to allow access from subclass</li>
 * <li>allow wildcard event handler</li>
 * </ul>
 */
public class EventBus {
    protected final Logger logger = Logger.getLogger(EventBus.class);
    /**
     * Im considering using {@link ConcurrentHashMap} here
     * it will reduce time to make map synchronized
     */
    final Map<String, List<EventHandler>> eventHandlers = new HashMap<String, List<EventHandler>>();

    ExecutorService executorService;

    @Autowired
    @Resource(name = "eventBusThreadPoolExecutor")
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        //TODO add default constructor in case no threadPool found
    }

    /**
     * fire an event with {@link EventException} allowed
     *
     * @param name
     * @param args
     * @throws EventException
     */
    public void fireEvent(String name, Object... args) throws EventException {
        Event event = new Event(name, args.length == 1 ? args[0] : args);

        fireEvent(event);
    }

    protected void fireEvent(Event event) throws EventException {
        if (logger.isDebugEnabled()) logger.debug("firing event " + event.getName() + " " + event.getData());

        fireNamedEvent(event.getName(), event);
        fireNamedEvent("*", event);
    }

    /**
     * fire event for handler listening on a specify event name
     *
     * @param name
     * @param event
     * @throws EventException
     */
    protected void fireNamedEvent(String name, Event event) throws EventException {
        ArrayList<EventHandler> handlers;
        synchronized (eventHandlers) {
            handlers = (ArrayList<EventHandler>) eventHandlers.get(name);
        }
        if (handlers == null) return;

        EventHandler[] arr;
        synchronized (handlers) {
            arr = handlers.toArray(new EventHandler[handlers.size()]);
        }

        for (EventHandler handler : arr) {
            if (logger.isTraceEnabled()) logger.trace("passing event " + event + " to " + handler);
            handler.handle(event);
        }
    }

    /**
     * fire an event and ignore {@link EventException} if any
     *
     * @param name
     * @param args
     * @return
     */
    public void fireSafeEvent(String name, Object... args) {
        if (args.length == 0) throw new IllegalArgumentException("event must have data");

        Event event = new Event(name, args.length == 1 ? args[0] : args);

        fireSafeEvent(event);
    }

    /**
     * fire an event and ignore {@link EventException} if any
     *
     * @param event
     */
    public void fireSafeEvent(Event event) {
        if (logger.isDebugEnabled()) logger.debug("firing event " + event.getName() + " " + event.getData());

        ArrayList<EventHandler> handlers;
        synchronized (eventHandlers) {
            handlers = (ArrayList<EventHandler>) eventHandlers.get(event.getName());
        }
        if (handlers == null) return;

        EventHandler[] arr;
        synchronized (handlers) {
            arr = handlers.toArray(new EventHandler[handlers.size()]);
        }

        for (EventHandler handler : arr) {
            if (logger.isTraceEnabled()) logger.trace("passing event " + event + " to " + handler);
            try {
                handler.handle(event);
            } catch (EventException ex) {
                logger.warn(handler + " throw exception for event " + event, ex);
            } catch (Throwable ex) {
                logger.error(handler + " throw unexpected exception for event " + event, ex);
            }
        }
    }

    /**
     * Fire an event and return immediately.
     * This method requires that an {@link ExecutorService} has been assign.
     *
     * @param name
     * @param args
     */
    public void fireAndForget(String name, Object... args) {
        if (executorService == null)
            //throw new IllegalArgumentException(String.format("executorService must not be null to handle event: %s", name));
            //just simply return to ignore this event
            return;
        Runnable eventRunnable = new EventRunnable(new Event(name, args.length == 1 ? args[0] : args));
        executorService.execute(eventRunnable);
        //      fireSafeEvent(name, args);
    }

    /**
     * subscribe to an event
     *
     * @param event
     * @param handler
     */
    public void subscribe(String event, EventHandler handler) {
        List<EventHandler> handlers = getHandlers(event, true);

        synchronized (handlers) {
            if (handlers.contains(handler)) {
                logger.warn("handler has already been subscribed " + handler);
                return;
            }
            handlers.add(handler);
        }
    }

    /**
     * unsubscribe to an event
     *
     * @param event
     * @param handler
     */
    public void unsubscribe(String event, EventHandler handler) {
        List<EventHandler> handlers = getHandlers(event, false);
        if (handlers == null || !handlers.contains(handler)) {
            logger.warn("handler has not been subscribed " + handler);
            return;
        }

        synchronized (handlers) {
            handlers.remove(handler);
        }
    }

    /**
     * get list of subscribed event handlers, or create a new one
     *
     * @param event
     * @param create
     * @return
     */
    protected List<EventHandler> getHandlers(String event, boolean create) {
        List<EventHandler> handlers;
        synchronized (eventHandlers) {
            handlers = eventHandlers.get(event);
            if (handlers == null && create) eventHandlers.put(event, handlers = new ArrayList<>());
        }
        return handlers;
    }

    /**
     * this runable to make event run async, if want to use this class, event data must not a consistence data
     * make sure to copy data object and pass to these event
     */
    class EventRunnable implements Runnable {
        Event event;

        public EventRunnable(Event e) {
            event = e;
        }

        @Override
        public void run() {
            fireSafeEvent(event);
        }

    }
}
