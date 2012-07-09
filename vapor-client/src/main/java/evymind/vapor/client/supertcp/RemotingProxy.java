package evymind.vapor.client.supertcp;

import evymind.vapor.core.Message;
import evymind.vapor.core.TransportChannel;

public class RemotingProxy {
	
	protected String interfaceName;
	protected Message message;
	protected TransportChannel transportChannel;
	
	public RemotingProxy(Message message, TransportChannel transportChannel) {
		this(null, message, transportChannel);
	}

	public RemotingProxy(String interfaceName, Message message, TransportChannel transportChannel) {
		this.interfaceName = interfaceName;
		this.message = message;
		this.transportChannel = transportChannel;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public Message getMessage() {
		return message;
	}

	public TransportChannel getTransportChannel() {
		return transportChannel;
	}

}
