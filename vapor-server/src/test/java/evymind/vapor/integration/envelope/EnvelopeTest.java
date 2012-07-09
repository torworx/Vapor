package evymind.vapor.integration.envelope;

import org.junit.Assert;
import org.junit.Test;

public class EnvelopeTest extends EnvelopeClentTestBase {
	
	@Test
	public void testSum() {
		Assert.assertEquals(3, service.sum(1, 2));
	}
	
}
