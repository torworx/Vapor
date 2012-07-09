package evymind.vapor.client.event;

import evymind.vapor.core.TransportChannel;

public class BeginProbeServersEvent extends AbstractTranportChannelEvent {

	public BeginProbeServersEvent(TransportChannel transportChannel) {
		super(transportChannel);
	}
	
}
