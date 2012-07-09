package evymind.vapor.deploy.providers;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import evymind.vapor.deploy.test.ConfiguredVapor;
import evymind.vapor.toolchain.test.TestingDir;

/**
 * Tests {@link ScanningAppProvider} as it starts up for the first time.
 */
public class ScanningAppProviderStartupTest {
	@Rule
	public TestingDir testdir = new TestingDir();
	private static ConfiguredVapor vapor;

	@Before
	public void setupEnvironment() throws Exception {
		vapor = new ConfiguredVapor(testdir);
		vapor.addConfiguration("vapor.ecs");
		vapor.addConfiguration("vapor-deploymgr-contexts.ecs");
		
		// Setup initial context
		vapor.copyContext("foo.ecs", "foo.ecs");
		vapor.copyApp("foo-app-1.sar", "foo.sar");

		// Should not throw an Exception
		vapor.load();

		// Start it
		vapor.start();
	}

	@After
	public void teardownEnvironment() throws Exception {
		// Stop vapor.
		vapor.stop();
	}

	@Test
	public void testStartupContext() {
		// Check Server for Handlers
		vapor.assertAppContextsExists("/foo");
	}
}
