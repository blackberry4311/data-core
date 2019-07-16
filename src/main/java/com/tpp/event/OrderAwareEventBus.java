package com.tpp.event;

import com.tpp.EventBus;
import com.tpp.EventHandler;
import com.tpp.persistence.util.BeanUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Resolve value from {@link Order} and {@link Ordered} to subscribe {@link EventHandler}
 */
public class OrderAwareEventBus extends EventBus {
	@Override
	public void subscribe(String event, EventHandler handler) {
		List<EventHandler> handlers = super.getHandlers(event, true);
		synchronized (handlers) {
			if (handlers.contains(handler)) {
				logger.warn("handler already subscribed " + handler);
				return;
			}

			Integer order = BeanUtils.getOrder(handler);
			if (order != null) {
				for (int i = 0; i < handlers.size(); i++) {
					EventHandler added = handlers.get(i);
					Integer ordered = BeanUtils.getOrder(added);
					if (ordered == null || ordered > order) {
						if (logger.isDebugEnabled())
							logger.debug("subscribed to " + event + " at index " + i + " for handler " + handler);
						handlers.add(i, handler);
						return;
					}
				}
			}
			if (logger.isDebugEnabled()) logger.debug("append to " + event + " for handler " + handler);
			handlers.add(handler);
		}
	}
}
