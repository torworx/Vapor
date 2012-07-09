package evymind.vapor.client.event;

import evymind.vapor.core.ServerLocator;
import evymind.vapor.core.TransportChannel;

public class BeginProbeServerEvent extends AbstractProbeServerEvent {

	public BeginProbeServerEvent(TransportChannel transportChannel, ServerLocator serverLocator) {
		super(transportChannel, serverLocator);
	}

}
