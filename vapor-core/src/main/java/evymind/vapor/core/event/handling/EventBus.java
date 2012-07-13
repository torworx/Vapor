package evymind.vapor.core.event.handling;

import evymind.vapor.core.QueueFullException;

public interface EventBus {
	
	/**
	 * Publish events to event bus. If the capacity of event bus is full, will throw {@link QueueFullException}.
	 * @param events
	 * @throws QueueFullException
	 */
	void publish(Object... events) throws QueueFullException;
	
	void subscribe(EventListener eventListener);
	
	void unsubscribe(EventListener eventListener);
	
	void unsubscribeAll();

}
