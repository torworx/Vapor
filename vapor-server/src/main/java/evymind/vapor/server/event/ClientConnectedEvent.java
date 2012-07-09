package evymind.vapor.server.event;

import java.util.UUID;

import evymind.vapor.core.Transport;

public class ClientConnectedEvent extends AbstractConnectionEvent<Transport> {

	public ClientConnectedEvent(Transport channel, UUID clientId) {
		super(channel, clientId);
	}

}
