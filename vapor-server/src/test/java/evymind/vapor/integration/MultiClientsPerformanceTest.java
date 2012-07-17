package evymind.vapor.integration;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Rule;
import org.junit.Test;

import evymind.vapor.service.api.MegaDemoService;

public class MultiClientsPerformanceTest {
	
	@Rule
	public ContiPerfRule i = new ContiPerfRule();
	
	private final ThreadLocal<ChannelClient> currentClient = new ThreadLocal<ChannelClient>();
	private final ThreadLocal<MegaDemoService> currentService = new ThreadLocal<MegaDemoService>();
	
	protected String getHost() {
		return "localhost";
	}
	
	protected int getPort() {
		return 8095;
	}
	
	protected ChannelClient getCurrentClient() {
		ChannelClient client = currentClient.get();
		if (client == null) {
			client = new ChannelClient(getHost(), getPort(), true);
			currentClient.set(client);
		}
		return client;
	}
	
	protected MegaDemoService getCurrentService() {
		MegaDemoService service = currentService.get();
		if (service == null) {
			service = getCurrentClient().createServiceProxy(MegaDemoService.class);
			currentService.set(service);
		}
		return service;
	}
	
	@Test
	@PerfTest(invocations = 100000, threads = 50)
	public void testSum() throws Exception {
		getCurrentService().sum(100, 200);
	}

}
