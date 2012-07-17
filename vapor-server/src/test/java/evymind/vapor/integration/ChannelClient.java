package evymind.vapor.integration;

import evymind.vapor.client.proxy.ServiceProxyFactory;
import evymind.vapor.client.proxy.utils.ServiceProxyUtils;
import evymind.vapor.client.supertcp.SuperTCPChannel;
import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.message.bin.BinMessageFactory;
import evymind.vapor.service.api.MegaDemoService;

public class ChannelClient {
	
	protected static int idx;
	protected int index;
	
	protected ServiceProxyFactory serviceProxyFactory = ServiceProxyUtils.getDefaultServiceProxyFactory();
	
	protected String host;
	protected int port;
	
	protected SuperTCPChannel channel;
	protected MegaDemoService megaDemoServiceProxy;
	protected MessageFactory<?> messageFactory = new BinMessageFactory();
	
	public ChannelClient(String host, int port, boolean connect) {
		this.host = host;
		this.port = port;
		index = ++idx;
		channel = new SuperTCPChannel();
		channel.setAckWaitTimeout(600000);
		megaDemoServiceProxy = serviceProxyFactory.getService(MegaDemoService.class, messageFactory, channel);
		if (connect) {
			connect();
		}
	}
	
	public void connect() {
		if (!isConnected()) {
			channel.connect(host, port);
		}
	}
	
	public boolean isConnected() {
		return channel != null && channel.isConnected();
	}
	
	public void disconnect() {
		if (isConnected()) {
			channel.disconnect();
		}
	}

	public SuperTCPChannel getChannel() {
		return channel;
	}
	
	public <T> T createServiceProxy(Class<T> serviceType) {
		return serviceProxyFactory.getService(serviceType, messageFactory, channel);
	}

}
