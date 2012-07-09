/*
 * Copyright (c) 2010-2012. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package evymind.vapor.core.event.handling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.core.event.Event;
import evymind.vapor.core.event.SimpleEvent;
import evymind.vapor.core.monitoring.jmx.JmxConfiguration;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Implementation of the {@link EventBus} that directly forwards all published
 * events (in the callers' thread) to all subscribed listeners.
 * <p/>
 * Listeners are expected to implement asynchronous handling themselves.
 * 
 * @author Allard Buijze
 * @see AsynchronousEventHandlerWrapper
 * @since 0.5
 */
public class SimpleEventBus implements EventBus {

	private static final Logger log = LoggerFactory.getLogger(SimpleEventBus.class);
	private final Set<EventListener> listeners = new CopyOnWriteArraySet<EventListener>();
	private volatile SimpleEventBusStatistics statistics = new SimpleEventBusStatistics();

	/**
	 * Initializes the SimpleEventBus and registers the mbeans for management
	 * information.
	 */
	public SimpleEventBus() {
		this(false);
	}

	/**
	 * Initiates the SimpleEventBus and makes the registration of mbeans for
	 * management information optional.
	 * 
	 * @param registerMBeans
	 *            true to register the mbeans, false for not registering them.
	 */
	public SimpleEventBus(boolean registerMBeans) {
		if (registerMBeans) {
			JmxConfiguration.getInstance().registerMBean(statistics, getClass());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void unsubscribe(EventListener eventListener) {
		Class<?> listenerType = getActualListenerType(eventListener);
		if (listeners.remove(eventListener)) {
			statistics.recordUnregisteredListener(listenerType.getSimpleName());
			log.debug("EventListener {} unsubscribed successfully", listenerType.getSimpleName());
		} else {
			log.info("EventListener {} not removed. It was already unsubscribed", listenerType.getSimpleName());
		}
	}

	@Override
	public void unsubscribeAll() {
		listeners.clear();
		statistics.recordUnregisteredAllListeners();
		log.debug("Unsubscribe all EventListeners successfully");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void subscribe(EventListener eventListener) {
		Class<?> listenerType = getActualListenerType(eventListener);
		if (listeners.add(eventListener)) {
			statistics.listenerRegistered(listenerType.getSimpleName());
			log.debug("EventListener [{}] subscribed successfully", listenerType.getSimpleName());
		} else {
			log.info("EventListener [{}] not added. It was already subscribed", listenerType.getSimpleName());
		}
	}

	private Class<?> getActualListenerType(EventListener eventListener) {
		Class<?> listenerType;
		if (eventListener instanceof EventListenerProxy) {
			listenerType = ((EventListenerProxy) eventListener).getTargetType();
		} else {
			listenerType = eventListener.getClass();
		}
		return listenerType;
	}

	@Override
	public void publish(Object... events) {
		statistics.recordPublishedEvent();
		if (!listeners.isEmpty()) {
			for (Object object : events) {
				Event<?> event = object instanceof Event ? (Event<?>) object : new SimpleEvent<Object>(object);
				for (EventListener listener : listeners) {
					if (log.isDebugEnabled()) {
						log.debug("Dispatching Event [{}] to EventListener [{}]", event.getPayloadType()
								.getSimpleName(),
								listener instanceof EventListenerProxy ? ((EventListenerProxy) listener)
										.getTargetType().getSimpleName() : listener.getClass()
										.getSimpleName());
					}
					listener.handle(event);
				}
			}
		}
	}

}
