package evymind.vapor.client.event;

import java.util.UUID;

import evymind.vapor.core.Transport;

public abstract class AbstractConnectionEvent<T extends Transport> {
	
	private final T channel;
	private final UUID clientId;
	
	public AbstractConnectionEvent(T channel, UUID clientId) {
		this.channel = channel;
		this.clientId = clientId;
	}
	
	public T getChannel() {
		return channel;
	}
	
	public UUID getClientId() {
		return clientId;
	}

	@Override
	public String toString() {
		return String.format("%s [%s]", getClass().getSimpleName(), getClientId());
	}

}
