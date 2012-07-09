package evymind.vapor;

import org.junit.Test;

import evymind.vapor.core.ProbingOption;
import evymind.vapor.core.ServerLocator;

public class ServerLocatorTest {

	@Test
	public void testServerLocatorToString() {
		ServerLocator locator = new ServerLocator();
		locator.setHost("192.168.1.93");
		locator.setPort(8095);
		locator.setProbingOptions(ProbingOption.DISABLE_IF_PROBE_FAILED);
		System.out.println(locator);
	}
}
