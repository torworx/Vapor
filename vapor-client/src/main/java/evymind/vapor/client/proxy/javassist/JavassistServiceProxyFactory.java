package evymind.vapor.client.proxy.javassist;

import java.util.Map;

import com.google.common.collect.Maps;

import evymind.vapor.client.proxy.ProxyFactory;
import evymind.vapor.client.proxy.ServiceProxyFactory;
import evymind.vapor.core.Message;
import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.TransportChannel;

public class JavassistServiceProxyFactory implements ServiceProxyFactory {
	
	private final Map<Class<?>, ProxyFactory> proxyFactories = Maps.newHashMap();
	
	public JavassistServiceProxyFactory() {
	}

	protected ProxyFactory createProxyFactory(Class<?> serviceIntf) {
		return new JavassistProxyFactoryBuilder().interfaces(serviceIntf).build();
	}
	
	protected ProxyFactory getProxyFactory(Class<?> serviceIntf) {
		ProxyFactory proxyFactory = proxyFactories.get(serviceIntf);
		if (proxyFactory == null) {
			proxyFactory = createProxyFactory(serviceIntf);
			proxyFactories.put(serviceIntf, proxyFactory);
		}
		return proxyFactory;
	}
	
	@Override
	public <T> T getService(Class<T> serviceIntf, MessageFactory<?> messageFactory, TransportChannel transportChannel) {
		return getProxyFactory(serviceIntf).getProxy(messageFactory, transportChannel);
	}

	@Override
	public <T> T getService(Class<T> serviceIntf, Message message, TransportChannel transportChannel) {
		return getProxyFactory(serviceIntf).getProxy(message, transportChannel);
	}

}
