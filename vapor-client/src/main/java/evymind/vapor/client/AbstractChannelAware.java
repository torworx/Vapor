package evymind.vapor.client;

import evymind.vapor.core.AbstractMessageFactoryAware;
import evymind.vapor.core.TransportChannel;

public class AbstractChannelAware extends AbstractMessageFactoryAware implements ChannelAware {

	private TransportChannel channel;

	public TransportChannel getChannel() {
		return channel;
	}

	public void setChannel(TransportChannel channel) {
		this.channel = channel;
	}
	
}
