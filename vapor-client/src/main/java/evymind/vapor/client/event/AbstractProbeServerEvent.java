package evymind.vapor.client.event;

import evymind.vapor.core.ServerLocator;
import evymind.vapor.core.TransportChannel;

public abstract class AbstractProbeServerEvent extends AbstractTranportChannelEvent {
	
	private final ServerLocator serverLocator;

	public AbstractProbeServerEvent(TransportChannel transportChannel, ServerLocator serverLocator) {
		super(transportChannel);
		this.serverLocator = serverLocator;
	}

	public ServerLocator getServerLocator() {
		return serverLocator;
	}

}
