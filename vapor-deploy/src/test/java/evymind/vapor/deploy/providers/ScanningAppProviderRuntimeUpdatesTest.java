package evymind.vapor.deploy.providers;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.core.utils.Scanner;
import evymind.vapor.deploy.AppProvider;
import evymind.vapor.deploy.DeploymentManager;
import evymind.vapor.deploy.test.Invoker;
import evymind.vapor.deploy.test.ConfiguredVapor;
import evymind.vapor.toolchain.test.OS;
import evymind.vapor.toolchain.test.TestingDir;

/**
 * Similar in scope to {@link ScanningAppProviderStartupTest}, except is concerned with the modification of existing
 * deployed apps due to incoming changes identified by the {@link ScanningAppProvider}.
 */
public class ScanningAppProviderRuntimeUpdatesTest {
	
	private static final Logger log = LoggerFactory.getLogger(ScanningAppProviderRuntimeUpdatesTest.class);

	@Rule
	public TestingDir testdir = new TestingDir();
	
	private static ConfiguredVapor vapor;
	private final AtomicInteger scans = new AtomicInteger();
	private int providers;

	@Before
	public void setupEnvironment() throws Exception {
		vapor = new ConfiguredVapor(testdir);
		vapor.addConfiguration("vapor.ecs");
		vapor.addConfiguration("vapor-deploymgr-contexts.ecs");

		// Should not throw an Exception
		vapor.load();

		// Start it
		vapor.start();

		// monitor tick
		DeploymentManager dm = vapor.getServer().getBeans(DeploymentManager.class).get(0);
		for (AppProvider provider : dm.getAppProviders()) {
			if (provider instanceof ScanningAppProvider) {
				providers++;
				((ScanningAppProvider) provider).addScannerListener(new Scanner.ScanListener() {
					public void scan() {
						scans.incrementAndGet();
					}
				});
			}
		}

	}

	@After
	public void teardownEnvironment() throws Exception {
		// Stop vapor.
		vapor.stop();
	}

	public void waitForDirectoryScan() {
		int scan = scans.get() + 2 * providers;
		do {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				log.warn(e.getMessage(), e);
			}
		} while (scans.get() < scan);
	}

	/**
	 * Simple app deployment after startup of server.
	 */
	@Test
	public void testAfterStartupContext() throws IOException {
		vapor.copyApp("foo-app-1.sar", "foo.sar");
		vapor.copyContext("foo.ecs", "foo.ecs");

		waitForDirectoryScan();
		waitForDirectoryScan();

		vapor.assertAppContextsExists("/foo");
	}

	/**
	 * Simple app deployment after startup of server, and then removal of the app.
	 */
	@Test
	public void testAfterStartupThenRemoveContext() throws IOException {
		vapor.copyApp("foo-app-1.sar", "foo.sar");
		vapor.copyContext("foo.ecs", "foo.ecs");

		waitForDirectoryScan();
		waitForDirectoryScan();

		vapor.assertAppContextsExists("/foo");

		vapor.removeContext("foo.ecs");

		waitForDirectoryScan();
		waitForDirectoryScan();

		// FIXME: hot undeploy with removal not working! - vapor.assertNoAppContexts();
	}

	/**
	 * Simple app deployment after startup of server, and then removal of the app.
	 */
	@Test
	public void testAfterStartupThenUpdateContext() throws Exception {
		// This test will not work on Windows as second sar file would
		// not be written over the first one because of a file lock
		Assume.assumeTrue(!OS.IS_WINDOWS);
		Assume.assumeTrue(!OS.IS_OSX); // build server has issues with finding itself apparently

		vapor.copyApp("foo-app-1.sar", "foo.sar");
		vapor.copyContext("foo.ecs", "foo.ecs");

		waitForDirectoryScan();
		waitForDirectoryScan();

		vapor.assertAppContextsExists("/foo");

		// Test that app response contains "-1"
		vapor.assertResponseContains(new Invoker() {
			
			@Override
			public Object invoke() {
				return vapor.getServiceProxyInvoker().invoke("evymind.vapor.core.tests.app.InfoService", "getInfo", String.class);
			}
		}, "FooService-1");

		waitForDirectoryScan();
		System.out.println("Updating sar files");
		vapor.copyContext("foo.ecs", "foo.ecs"); // essentially "touch" the context xml
		vapor.copyApp("foo-app-2.sar", "foo.sar");

		// This should result in the existing foo.sar being replaced with the new foo.sar
		waitForDirectoryScan();
		waitForDirectoryScan();
		vapor.assertAppContextsExists("/foo");

		// Test that app response contains "-2"
		vapor.assertResponseContains(new Invoker() {
			
			@Override
			public Object invoke() {
				return vapor.getServiceProxyInvoker().invoke("evymind.vapor.core.tests.app.InfoService", "getInfo", String.class);
			}
		}, "FooService-2");
	}
}
