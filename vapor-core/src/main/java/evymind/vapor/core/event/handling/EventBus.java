package evymind.vapor.core.event.handling;

public interface EventBus {
	
	void publish(Object... events);
	
	void subscribe(EventListener eventListener);
	
	void unsubscribe(EventListener eventListener);
	
	void unsubscribeAll();

}
