package evymind.vapor.client.proxy;

import evymind.vapor.core.Message;
import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.TransportChannel;

public interface ServiceProxyFactory {
	
	<T> T getService(Class<T> serviceIntf, MessageFactory<?> messageFactory, TransportChannel transportChannel);

	<T> T getService(Class<T> serviceIntf, Message message, TransportChannel transportChannel);
}
