package evymind.vapor.client.event;

import evymind.vapor.core.TransportChannel;

public class EndProbeServersEvent extends AbstractTranportChannelEvent {
	 
	private final int probedCount;
	
	private final int enabledCount;
	
	private final int disabledCount;

	public EndProbeServersEvent(TransportChannel transportChannel, int probedCount, int enabledCount, int disabledCount) {
		super(transportChannel);
		this.probedCount = probedCount;
		this.enabledCount = enabledCount;
		this.disabledCount = disabledCount;
	}

	public int getProbedCount() {
		return probedCount;
	}

	public int getEnabledCount() {
		return enabledCount;
	}

	public int getDisabledCount() {
		return disabledCount;
	}
	
}
