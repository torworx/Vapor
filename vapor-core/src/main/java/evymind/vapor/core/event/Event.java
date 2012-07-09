package evymind.vapor.core.event;

public interface Event<T> {
	
	T getPayload();
	
	Class<?> getPayloadType();

}
