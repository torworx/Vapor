package evymind.vapor.deploy;

import java.util.Collection;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import evymind.vapor.deploy.test.ConfiguredVapor;
import evymind.vapor.toolchain.test.TestingDir;

public class DeploymentManagerTest {
	@Rule
	public TestingDir testdir = new TestingDir();

	@Test
	public void testReceiveApp() throws Exception {
		DeploymentManager depman = new DeploymentManager();
		depman.setDefaultLifecycleGoal(null); // no default
		AppLifecyclePathCollector pathtracker = new AppLifecyclePathCollector();
		MockAppProvider mockProvider = new MockAppProvider();

		depman.addLifecycleBinding(pathtracker);
		depman.addAppProvider(mockProvider);

		// Start DepMan
		depman.start();

		// Trigger new App
		mockProvider.findWebapp("foo-app-1.sar");

		// Test app tracking
		Collection<App> apps = depman.getApps();
		Assert.assertNotNull("Should never be null", apps);
		Assert.assertEquals("Expected App Count", 1, apps.size());

		// Test app get
		App actual = depman.getAppByOriginId("mock-foo-app-1.sar");
		Assert.assertNotNull("Should have gotten app (by id)", actual);
		Assert.assertEquals("Should have gotten app (by id)", "mock-foo-app-1.sar", actual.getOriginId());
	}

	@Test
	public void testBinding() {
		AppLifecyclePathCollector pathtracker = new AppLifecyclePathCollector();
		DeploymentManager depman = new DeploymentManager();
		depman.addLifecycleBinding(pathtracker);

		Set<AppLifecycle.Binding> allbindings = depman.getLifecycle().getBindings();
		Assert.assertNotNull("All Bindings should never be null", allbindings);
		Assert.assertEquals("All Bindings.size", 1, allbindings.size());

		Set<AppLifecycle.Binding> deploybindings = depman.getLifecycle().getBindings("deploying");
		Assert.assertNotNull("'deploying' Bindings should not be null", deploybindings);
		Assert.assertEquals("'deploying' Bindings.size", 1, deploybindings.size());
	}

	@Test
	public void testXmlConfigured() throws Exception {
		ConfiguredVapor vapor = null;
		try {
			vapor = new ConfiguredVapor(testdir);
			vapor.addConfiguration("vapor.ecs");
			vapor.addConfiguration("vapor-deploymgr-contexts.ecs");

			// Should not throw an Exception
			vapor.load();

			// Start it
			vapor.start();
		} finally {
			if (vapor != null) {
				try {
					vapor.stop();
				} catch (Exception ignore) {
					// ignore
				}
			}
		}
	}
}
