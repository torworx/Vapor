package evymind.vapor.integration.envelope;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class EnvelopePerformanceTest extends EnvelopeClentTestBase {
	
	@Rule
	public ContiPerfRule i = new ContiPerfRule();

	
	@Test
	@PerfTest(invocations = 100000)
	public void testSum() {
		Assert.assertEquals(3, service.sum(1, 2));
	}
}
