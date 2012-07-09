package evymind.vapor.client.event;

import java.util.UUID;

import evymind.vapor.core.Transport;

public class ChannelDisconnectedEvent extends AbstractConnectionEvent<Transport> {

	public ChannelDisconnectedEvent(Transport channel, UUID clientId) {
		super(channel, clientId);
	}

}
