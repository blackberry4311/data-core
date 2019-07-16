package com.tpp.persistence.util;

import com.tpp.Event;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.annotation.OrderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BeanUtils {

	/**
	 * get order value from {@link Order} or {@link Ordered}
	 *
	 * @param obj
	 * @return
	 */
	public static Integer getOrder(Object obj) {
		if (obj instanceof Ordered) {
			return ((Ordered) obj).getOrder();
		}

		return OrderUtils.getOrder(obj.getClass());
	}

	/**
	 * this method to make sure {@link Event} should be handle with exactly Handler
	 *
	 * @param props
	 * @param appCtx
	 * @param <T>
	 * @return
	 */
	public static <T> T getWithProperties(Properties props, ApplicationContext appCtx) {

		// initiate with bean instance
		String beanId = props.getProperty("-bean-id");
		if (StringUtils.isNotBlank(beanId)) {
			@SuppressWarnings("unchecked") T obj = (T) appCtx.getBean(beanId);
			return obj;
		}

		// initiate with simple bean definition
		String beanClass = props.getProperty("-bean-class");
		if (StringUtils.isNotBlank(beanClass)) {
			try {
				@SuppressWarnings("unchecked") Class<T> klass = (Class<T>) appCtx.getClassLoader().loadClass(beanClass);
				List<Object> params = new ArrayList<Object>();
				for (int i = 1; ; i++) {
					String propArg = "-bean-arg-" + i;
					if (!props.containsKey(propArg)) break;
					String arg = props.getProperty(propArg);
					// TODO resolve argument type
					params.add(arg);
				}
				Class<?>[] types = new Class<?>[params.size()];
				for (int i = 0; i < params.size(); i++)
					types[i] = params.get(i).getClass();
				T obj = klass.getConstructor(types).newInstance(params.toArray(new Object[params.size()]));
				return obj;
			} catch (Throwable ex) {
				throw new RuntimeException("failed to create bean from class: " + beanClass, ex);
			}
		}

		return null;
	}

}
