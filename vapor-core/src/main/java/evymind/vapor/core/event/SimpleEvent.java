package evymind.vapor.core.event;

public class SimpleEvent<T> implements Event<T> {
	
	private T payload;
	private Class<?> payloadType;

	public SimpleEvent(T payload) {
		this.payload = payload;
		this.payloadType = payload.getClass();
	}

	@Override
	public T getPayload() {
		return this.payload;
	}

	@Override
	public Class<?> getPayloadType() {
		return this.payloadType;
	}

}
