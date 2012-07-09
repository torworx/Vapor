package evymind.vapor.integration.envelope;

import org.junit.After;
import org.junit.Before;

import evymind.vapor.client.proxy.ServiceProxyFactory;
import evymind.vapor.client.proxy.utils.ServiceProxyUtils;
import evymind.vapor.client.supertcp.SuperTCPChannel;
import evymind.vapor.service.api.MegaDemoService;

public class EnvelopeClentTestBase extends EnvelopeTestBase {
	
	protected ServiceProxyFactory serviceProxyFactory = ServiceProxyUtils.getDefaultServiceProxyFactory();
	
	protected SuperTCPChannel channel;
	protected MegaDemoService service;
	
	@Before
	public void setUp() {
		initMessageFactory();
		
		channel = new SuperTCPChannel();
		// For debug
		channel.setAckWaitTimeout(600000);
		channel.connect("127.0.0.1");
		service = serviceProxyFactory.getService(MegaDemoService.class, messageFactory, channel);
	}
	
	@After
	public void tearDown() {
		channel.disconnect();
	}
}
