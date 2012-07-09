package evymind.vapor.client.proxy;

import evymind.vapor.core.Message;
import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.TransportChannel;

public interface ProxyFactory {
	
	<T> T getProxy(MessageFactory<?> messageFactory, TransportChannel transportChannel);
	
	<T> T getProxy(Message message, TransportChannel transportChannel);

}
