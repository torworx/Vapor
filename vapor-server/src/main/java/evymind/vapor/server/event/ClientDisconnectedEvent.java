package evymind.vapor.server.event;

import java.util.UUID;

import evymind.vapor.core.Transport;

public class ClientDisconnectedEvent extends AbstractConnectionEvent<Transport> {

	public ClientDisconnectedEvent(Transport channel, UUID clientId) {
		super(channel, clientId);
	}

}
