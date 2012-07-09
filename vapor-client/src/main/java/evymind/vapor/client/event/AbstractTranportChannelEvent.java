package evymind.vapor.client.event;

import evymind.vapor.core.TransportChannel;

public class AbstractTranportChannelEvent {
	
	private final TransportChannel transportChannel;

	public AbstractTranportChannelEvent(TransportChannel transportChannel) {
		this.transportChannel = transportChannel;
	}

	public TransportChannel getTransportChannel() {
		return transportChannel;
	}

}
