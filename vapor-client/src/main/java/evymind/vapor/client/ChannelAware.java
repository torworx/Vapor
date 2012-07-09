package evymind.vapor.client;

import evymind.vapor.core.TransportChannel;

public interface ChannelAware {
	
	TransportChannel getChannel();
	
	void setChannel(TransportChannel channel);

}
