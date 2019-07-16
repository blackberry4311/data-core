package com.tpp.event;

import com.tpp.Event;
import com.tpp.EventHandler;
import com.tpp.exception.EventException;
import org.apache.log4j.Logger;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.annotation.OrderUtils;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * this class provide an implementation which will apply event arguments on a method.
 */
public class MethodEventHandlerImpl implements EventHandler, Ordered {

	private static final Logger logger = Logger.getLogger(MethodEventHandlerImpl.class);

	Method method;

	Object instance;

	Class<?>[] argTypes;

	public static EventHandler create(Method method, Object instance) {
		MethodEventHandlerImpl handler = new MethodEventHandlerImpl();

		handler.method = method;
		handler.instance = instance;

		handler.argTypes = method.getParameterTypes();

		return handler;
	}

	/**
	 * prevent object creation
	 */
	private MethodEventHandlerImpl() {
	}

	@Override
	public void handle(Event event) throws EventException {
		Object data = event.getData();
		Object[] args = getEventArgs(data);

		if (!method.isVarArgs() && args.length != argTypes.length) {
			if (logger.isDebugEnabled()) logger.debug(
					"adjust event arguments of " + event.getName() + " to " + argTypes.length + " for "
							+ getMethodName());

			args = Arrays.copyOf(args, argTypes.length);
		}

		try {
			method.invoke(instance, args);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new EventException("error applying event arguments on " + getMethodName(), e);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getTargetException();
			if (cause instanceof EventException) throw (EventException) cause;
			throw new EventException("Unexpected event exception from " + getMethodName(), cause);
		}
	}

	private String getMethodName() {
		return instance.getClass().getName() + "." + method.getName();
	}

	private static Object[] getEventArgs(Object data) {
		Object[] args;
		if (data instanceof Object[]) {
			args = (Object[]) data;
		} else {
			Class<?> type = data.getClass();
			if (type.isArray()) {
				args = new Object[Array.getLength(data)];
				for (int i = 0; i < args.length; i++)
					args[i] = Array.get(data, i);
			} else {
				args = new Object[] { data };
			}
		}
		return args;
	}

	@Override
	public int getOrder() {
		Order orderVal = method.getAnnotation(Order.class);
		if (orderVal != null) return orderVal.value();

		if (instance instanceof Ordered) return ((Ordered) instance).getOrder();

		Integer order = OrderUtils.getOrder(instance.getClass());
		if (order != null) return order;

		return Integer.MAX_VALUE;
	}

}
