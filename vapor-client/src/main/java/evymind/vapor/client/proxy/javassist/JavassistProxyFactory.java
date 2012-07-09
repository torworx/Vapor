package evymind.vapor.client.proxy.javassist;

import evymind.vapor.client.proxy.ProxyFactory;
import evymind.vapor.core.Message;
import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.TransportChannel;

public class JavassistProxyFactory implements ProxyFactory {

	private Class<?> factory;
	private Class<?> superclass;
	private Class<?>[] interfaces;

	public JavassistProxyFactory(Class<?> factory, Class<?> superclass, Class<?>[] interfaces) {
		this.factory = factory;
		this.superclass = superclass;
		this.interfaces = interfaces;
	}

	@Override
	public <T> T getProxy(MessageFactory<?> messageFactory, TransportChannel transportChannel) {
		return getProxy(messageFactory.createMessage(), transportChannel);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProxy(Message message, TransportChannel transportChannel) {
		return (T) JavassistProxyHandler.getProxy(superclass, interfaces, factory, message, transportChannel);
	}

}
