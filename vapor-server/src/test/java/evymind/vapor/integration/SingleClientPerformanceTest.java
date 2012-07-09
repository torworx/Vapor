package evymind.vapor.integration;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import evymind.vapor.client.proxy.ServiceProxyFactory;
import evymind.vapor.client.proxy.utils.ServiceProxyUtils;
import evymind.vapor.client.supertcp.SuperTCPChannel;
import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.message.bin.BinMessageFactory;
import evymind.vapor.service.api.MegaDemoService;

public class SingleClientPerformanceTest {
	
	protected ServiceProxyFactory serviceProxyFactory = ServiceProxyUtils.getDefaultServiceProxyFactory();

	private boolean printInfo = false;
	
	private SuperTCPChannel channel;
	private MegaDemoService megaDemoServiceProxy;
	private MessageFactory<?> messageFactory = new BinMessageFactory();
	
	@Rule
	public ContiPerfRule i = new ContiPerfRule();
	
	@Before
	public void setup() throws InterruptedException {
		printInfo = false;
		
		channel = new SuperTCPChannel();
		// For debug
		channel.setAckWaitTimeout(600000);
		channel.connect("localhost");
		
		megaDemoServiceProxy = serviceProxyFactory.getService(MegaDemoService.class, messageFactory, channel);
	}
	
	@After
	public void teardown() throws InterruptedException {
		channel.disconnect();
	}
	
	@Test
	@PerfTest(invocations = 100000)
	public void testSum() {
		if (printInfo) {
			System.out.println("--------------------------------------------");
			System.out.println("                 testSum                    ");
			System.out.println("--------------------------------------------");
		}
		megaDemoServiceProxy.sum(1, 2);
	}
}
