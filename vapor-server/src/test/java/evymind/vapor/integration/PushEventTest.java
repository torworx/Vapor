package evymind.vapor.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import evymind.vapor.client.eventreceiver.DefaultEventReceiver;
import evymind.vapor.client.proxy.ServiceProxyFactory;
import evymind.vapor.client.proxy.utils.ServiceProxyUtils;
import evymind.vapor.client.supertcp.SuperTCPChannel;
import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.event.handling.annontation.EventHandler;
import evymind.vapor.core.message.bin.BinMessageFactory;
import evymind.vapor.service.api.MegaDemoService;
import evymind.vapor.service.api.TimeEvent;

public class PushEventTest {
	
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
		
		eventReceiver = new DefaultEventReceiver();
		eventReceiver.setMessageFactory(messageFactory);
		channel.registerEventReceiver(eventReceiver);
		
		eventReceiver.subscribe(new TimeEventHandler());
		
		channel.connect("127.0.0.1");
		service = serviceProxyFactory.getService(MegaDemoService.class, messageFactory, channel);
	}
	
	@After
	public void tearDown() {
		channel.disconnect();
	}
	
	@Test
	public void testTimeEvent() throws InterruptedException {
		service.subscribeTime(1000);
		Thread.sleep(60*1000);
	}
	
	class TimeEventHandler {
		
		@EventHandler
		public void handleTimeEvent(TimeEvent event) {
			System.out.println(event.getTime());
		}
	}
}
