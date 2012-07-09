package evymind.vapor.client.event;

import evymind.vapor.core.ServerLocator;
import evymind.vapor.core.TransportChannel;

public class EndProbeServerEvent extends AbstractProbeServerEvent {
	
	private final boolean fail;

	public EndProbeServerEvent(TransportChannel transportChannel, ServerLocator serverLocator, boolean fail) {
		super(transportChannel, serverLocator);
		this.fail = fail;
	}

	public boolean isFail() {
		return fail;
	}
	
}
