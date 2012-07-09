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

package evymind.vapor.core.event.handling.annontation;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import evymind.vapor.core.event.Event;
import evymind.vapor.core.event.Subscribable;
import evymind.vapor.core.event.handling.EventBus;
import evymind.vapor.core.event.handling.EventListener;
import evymind.vapor.core.event.handling.EventListenerProxy;

/**
 * Adapter that turns any bean with {@link EventHandler} annotated methods into
 * an {@link org.axonframework.eventhandling.EventListener}.
 * <p/>
 * If the event listener has the {@link AsynchronousEventListener} annotation,
 * it is also configured to handle events asynchronously. In that case, event
 * processing is handed over to the given {@link java.util.concurrent.Executor}.
 * 
 * @author Allard Buijze
 * @see EventListener
 * @see org.axonframework.eventhandling.AsynchronousEventHandlerWrapper
 * @since 0.1
 */
@SuppressWarnings("rawtypes")
public class AnnotationEventListenerAdapter implements Subscribable, EventListenerProxy {

	private final EventListener targetEventListener;
	private final EventBus eventBus;
	private final Class<?> listenerType;

	/**
	 * Subscribe the given <code>annotatedEventListener</code> to the given
	 * <code>eventBus</code>.
	 * 
	 * @param annotatedEventListener
	 *            The annotated event listener
	 * @param eventBus
	 *            The event bus to subscribe to
	 * @return an AnnotationEventListenerAdapter that wraps the listener. Can be
	 *         used to unsubscribe.
	 */
	public static AnnotationEventListenerAdapter subscribe(Object annotatedEventListener, EventBus eventBus) {
		AnnotationEventListenerAdapter adapter = new AnnotationEventListenerAdapter(annotatedEventListener, eventBus);
		adapter.subscribe();
		return adapter;
	}

	/**
	 * Initialize the AnnotationEventListenerAdapter for the given
	 * <code>annotatedEventListener</code>. If the
	 * <code>annotatedEventListener</code> is asynchronous (has the
	 * {@link AsynchronousEventListener}) annotation) then the given executor is
	 * used to execute event processing.
	 * 
	 * @param annotatedEventListener
	 *            the event listener
	 * @param executor
	 *            The executor to use when wiring an Asynchronous Event
	 *            Listener.
	 * @param eventBus
	 *            the event bus to register the event listener to
	 */
	public AnnotationEventListenerAdapter(Object annotatedEventListener, EventBus eventBus) {
		this.listenerType = annotatedEventListener.getClass();
		EventListener adapter = new TargetEventListener(new AnnotationEventHandlerInvoker(annotatedEventListener));
		this.eventBus = eventBus;
		this.targetEventListener = adapter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(Event event) {
		targetEventListener.handle(event);
	}

	/**
	 * Unsubscribe the EventListener with the configured EventBus.
	 */
	@Override
	@PreDestroy
	public void unsubscribe() {
		eventBus.unsubscribe(this);
	}

	/**
	 * Subscribe the EventListener with the configured EventBus.
	 */
	@Override
	@PostConstruct
	public void subscribe() {
		eventBus.subscribe(this);
	}

	@Override
	public Class<?> getTargetType() {
		return listenerType;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AnnotationEventListenerAdapter)) {
			return false;
		}
		AnnotationEventListenerAdapter target = (AnnotationEventListenerAdapter) obj;
		
		return this.targetEventListener.equals(target.targetEventListener);
	}

	private static final class TargetEventListener implements EventListener {

		private final AnnotationEventHandlerInvoker eventHandlerInvoker;

		public TargetEventListener(AnnotationEventHandlerInvoker eventHandlerInvoker) {
			this.eventHandlerInvoker = eventHandlerInvoker;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handle(Event event) {
			eventHandlerInvoker.invokeEventHandlerMethod(event);
		}
	}
}
