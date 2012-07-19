package evymind.vapor.deploy.providers;

import java.io.File;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import evymind.vapor.deploy.test.ConfiguredVapor;
import evymind.vapor.toolchain.test.TestingDir;

public class DefaultAppProviderTest {
	
	@Rule
	public TestingDir testdir = new TestingDir();
	private static ConfiguredVapor vapor;

	@Before
	public void setupEnvironment() throws Exception {
		vapor = new ConfiguredVapor(testdir);
		vapor.addConfiguration("vapor.ecs");
		vapor.addConfiguration("vapor-deploy-sars.ecs");

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

		File workDir = vapor.getVaporDir("workish");

		System.err.println("workDir=" + workDir);

		// Test for correct behaviour
		Assert.assertTrue("Should have generated directory in work directory: " + workDir,
				hasVaporGeneratedPath(workDir, "foo.sar"));
	}

	private static boolean hasVaporGeneratedPath(File basedir, String expectedWarFilename) {
		for (File path : basedir.listFiles()) {
			if (path.exists() && path.isDirectory() && path.getName().startsWith("vapor-")
					&& path.getName().contains(expectedWarFilename)) {
				System.out.println("Found expected generated directory: " + path);
				return true;
			}
		}

		System.err.println("did not find " + expectedWarFilename + " in " + Arrays.asList(basedir.listFiles()));
		return false;
	}

	public static void assertDirNotExists(String msg, File workDir, String subdir) {
		File dir = new File(workDir, subdir);
		Assert.assertFalse("Should not have " + subdir + " in " + msg + " - " + workDir, dir.exists());
	}
}
