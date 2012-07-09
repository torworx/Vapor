package evymind.vapor.integration;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import evymind.vapor.client.eventreceiver.DefaultEventReceiver;
import evymind.vapor.client.proxy.ServiceProxyFactory;
import evymind.vapor.client.proxy.utils.ServiceProxyUtils;
import evymind.vapor.client.supertcp.SuperTCPChannel;
import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.message.bin.BinMessageFactory;
import evymind.vapor.service.api.MegaDemoService;

public class SimpleServiceTest {
	
	protected ServiceProxyFactory serviceProxyFactory = ServiceProxyUtils.getDefaultServiceProxyFactory();
	
	protected SuperTCPChannel channel;
	protected DefaultEventReceiver eventReceiver;
	protected MessageFactory<?> messageFactory = new BinMessageFactory();
	protected MegaDemoService service;
	
	@Before
	public void setUp() {
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
	
	@Test
	public void testSum() {
		service.sum(1, 2);
	}
	
	@Test
	public void testGetList() {
		String[] expected = new String[]{"Hello", "World"};
		List<String> result = service.getList("Hello", "World");
		Assert.assertArrayEquals(expected, result.toArray());
	}
	
	@Test
	public void testSession() {
		String key = "message";
		String value = "Hello World!";
		service.setSessionValue(key, value);
		Assert.assertEquals(value, service.getSessionValue(key));
	}

	@Test
	public void testEcho() {
		Assert.assertEquals("Hello World!", service.echo("Hello World!"));
	}
	
	@Test
	public void testLongMessageEcho() {
		StringBuilder longMessage = new StringBuilder();
		for (int i = 0; i < 10000; i++) {
			longMessage.append(getClass().getName());
		}
		String result = service.echo(longMessage.toString());
		Assert.assertEquals(longMessage.toString(), result);
	}

}
