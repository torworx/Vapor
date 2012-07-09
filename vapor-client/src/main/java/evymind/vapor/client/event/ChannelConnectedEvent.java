package evymind.vapor.client.event;

import java.util.UUID;

import evymind.vapor.core.Transport;

public class ChannelConnectedEvent extends AbstractConnectionEvent<Transport> {

	public ChannelConnectedEvent(Transport channel, UUID clientId) {
		super(channel, clientId);
	}

}
