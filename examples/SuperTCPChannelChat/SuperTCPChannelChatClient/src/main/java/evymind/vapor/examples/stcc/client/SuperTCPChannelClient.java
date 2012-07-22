package evymind.vapor.examples.stcc.client;

import java.util.Map;

import com.google.common.collect.Maps;

import evymind.vapor.client.eventreceiver.DefaultEventReceiver;
import evymind.vapor.client.eventreceiver.EventReceiver;
import evymind.vapor.client.proxy.ServiceProxyFactory;
import evymind.vapor.client.proxy.utils.ServiceProxyUtils;
import evymind.vapor.client.supertcp.SuperTCPChannel;
import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.message.bin.BinMessageFactory;

public class SuperTCPChannelClient {
	
	protected ServiceProxyFactory serviceProxyFactory = ServiceProxyUtils.getDefaultServiceProxyFactory();
	
	protected String host;
	protected int port;
	
	protected SuperTCPChannel channel;
	protected MessageFactory<?> messageFactory; 
	protected DefaultEventReceiver eventReceiver;
	
	protected Map<Object, Object> services = Maps.newHashMap();
	
	public SuperTCPChannelClient() {
		this("localhost");
	}

	public SuperTCPChannelClient(String host) {
		this(host, 8095);
	}

	public SuperTCPChannelClient(String host, int port) {
		this.host = host;
		this.port = port;
		channel = new SuperTCPChannel();
		messageFactory = new BinMessageFactory();;
		
		eventReceiver = new DefaultEventReceiver();
		eventReceiver.setMessageFactory(messageFactory);
		channel.registerEventReceiver(eventReceiver);
	}

	public boolean isConnected() {
		return channel != null && channel.isConnected();
	}
	
	public void connect() {
		if (!isConnected()) {
			channel.connect(host, port);
		}
	}
	
	public void disconnect() {
		if (isConnected()) {
			channel.disconnect();
		}
	}

	public SuperTCPChannel getChannel() {
		return channel;
	}
	
	public EventReceiver getEventReceiver() {
		return eventReceiver;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getService(Class<T> serviceInterface) {
		Object service = services.get(serviceInterface);
		if (service == null) {
			service = serviceProxyFactory.getService(serviceInterface, messageFactory, channel);
			services.put(serviceInterface, service);
		}
		return (T) service;
	}
	
	public void subscribe(Object eventHandler) {
		eventReceiver.subscribe(eventHandler);
	}
	
	public void unsubscribe(Object eventHandler) {
		eventReceiver.unsubscribe(eventHandler);
	}
}
