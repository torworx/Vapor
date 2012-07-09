package evymind.vapor.core.event.handling.disruptor;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;

import evymind.vapor.core.event.Event;
import evymind.vapor.core.event.SimpleEvent;
import evymind.vapor.core.event.handling.EventListener;
import evymind.vapor.core.event.handling.EventListenerProxy;

public class EventHandlerInvoker implements EventHandler<EventHandlingEntry> {

	private static final Logger logger = LoggerFactory.getLogger(EventHandlerInvoker.class);

	private final Set<EventListener> listeners;

	public EventHandlerInvoker(Set<EventListener> listeners) {
		this.listeners = listeners;
	}

	@Override
	public void onEvent(EventHandlingEntry event, long sequence, boolean endOfBatch) throws Exception {
		process(event.getEvents());
	}

	protected void process(Object... events) {
		if (!listeners.isEmpty()) {
			for (Object object : events) {
				Event<?> event = object instanceof Event ? (Event<?>) object : new SimpleEvent<Object>(object);
				for (EventListener listener : listeners) {
					if (logger.isDebugEnabled()) {
						logger.debug("Dispatching Event [{}] to EventListener [{}]", event.getPayloadType()
								.getSimpleName(),
								listener instanceof EventListenerProxy ? ((EventListenerProxy) listener)
										.getTargetType().getSimpleName() : listener.getClass().getSimpleName());
					}
					listener.handle(event);
				}
			}
		}
	}

}
