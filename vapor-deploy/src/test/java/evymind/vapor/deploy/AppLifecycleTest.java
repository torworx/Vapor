package evymind.vapor.deploy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import evymind.vapor.deploy.graph.GraphOutputDot;
import evymind.vapor.deploy.graph.Node;
import evymind.vapor.deploy.graph.Path;
import evymind.vapor.toolchain.test.TestingDir;

/**
 * Just an overly picky test case to validate the potential paths.
 */
public class AppLifecycleTest {
	@Rule
	public TestingDir testdir = new TestingDir();

	private void assertNoPath(String from, String to) {
		assertPath(from, to, new ArrayList<String>());
	}

	private void assertPath(AppLifecycle lifecycle, String from, String to, List<String> expected) {
		Node fromNode = lifecycle.getNodeByName(from);
		Node toNode = lifecycle.getNodeByName(to);
		Path actual = lifecycle.getPath(fromNode, toNode);
		String msg = "LifeCycle path from " + from + " to " + to;
		Assert.assertNotNull(msg + " should never be null", actual);

		if (expected.size() != actual.nodes()) {
			System.out.println();
			System.out.printf("/* from '%s' -> '%s' */%n", from, to);
			System.out.println("/* Expected Path */");
			for (String path : expected) {
				System.out.println(path);
			}
			System.out.println("/* Actual Path */");
			for (Node path : actual.getNodes()) {
				System.out.println(path.getName());
			}

			Assert.assertEquals(msg + " / count", expected.size(), actual.nodes());
		}

		for (int i = 0, n = expected.size(); i < n; i++) {
			Assert.assertEquals(msg + "[" + i + "]", expected.get(i), actual.getNode(i).getName());
		}
	}

	private void assertPath(String from, String to, List<String> expected) {
		AppLifecycle lifecycle = new AppLifecycle();
		assertPath(lifecycle, from, to, expected);
	}

	@Test
	public void testFindPath_Deployed_Deployed() {
		assertNoPath("deployed", "deployed");
	}

	@Test
	public void testFindPath_Deployed_Started() {
		List<String> expected = new ArrayList<String>();
		expected.add("deployed");
		expected.add("starting");
		expected.add("started");
		assertPath("deployed", "started", expected);
	}

	@Test
	public void testFindPath_Deployed_Undeployed() {
		List<String> expected = new ArrayList<String>();
		expected.add("deployed");
		expected.add("undeploying");
		expected.add("undeployed");
		assertPath("deployed", "undeployed", expected);
	}

	@Test
	public void testFindPath_Started_Deployed() {
		List<String> expected = new ArrayList<String>();
		expected.add("started");
		expected.add("stopping");
		expected.add("deployed");
		assertPath("started", "deployed", expected);
	}

	@Test
	public void testFindPath_Started_Started() {
		assertNoPath("started", "started");
	}

	@Test
	public void testFindPath_Started_Undeployed() {
		List<String> expected = new ArrayList<String>();
		expected.add("started");
		expected.add("stopping");
		expected.add("deployed");
		expected.add("undeploying");
		expected.add("undeployed");
		assertPath("started", "undeployed", expected);
	}

	@Test
	public void testFindPath_Undeployed_Deployed() {
		List<String> expected = new ArrayList<String>();
		expected.add("undeployed");
		expected.add("deploying");
		expected.add("deployed");
		assertPath("undeployed", "deployed", expected);
	}

	@Test
	public void testFindPath_Undeployed_Started() {
		List<String> expected = new ArrayList<String>();
		expected.add("undeployed");
		expected.add("deploying");
		expected.add("deployed");
		expected.add("starting");
		expected.add("started");
		assertPath("undeployed", "started", expected);
	}

	@Test
	public void testFindPath_Undeployed_Uavailable() {
		assertNoPath("undeployed", "undeployed");
	}

	/**
	 * Request multiple lifecycle paths with a single lifecycle instance. Just
	 * to ensure that there is no state maintained between
	 * {@link AppLifecycle#getPath(Node, Node)} requests.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testFindPathMultiple() throws IOException {
		AppLifecycle lifecycle = new AppLifecycle();
		List<String> expected = new ArrayList<String>();

		File outputDir = testdir.getEmptyDir();

		// Modify graph to add new 'staging' -> 'staged' between 'deployed' and
		// 'started'
		GraphOutputDot.write(lifecycle, new File(outputDir, "multiple-1.dot")); // before
																				// change
		lifecycle.insertNode(lifecycle.getPath("deployed", "started").getEdge(0), "staging");
		GraphOutputDot.write(lifecycle, new File(outputDir, "multiple-2.dot")); // after
																				// first
																				// change
		lifecycle.insertNode(lifecycle.getPath("staging", "started").getEdge(0), "staged");
		GraphOutputDot.write(lifecycle, new File(outputDir, "multiple-3.dot")); // after
																				// second
																				// change

		// Deployed -> Deployed
		expected.clear();
		assertPath(lifecycle, "deployed", "deployed", expected);

		// Deployed -> Staged
		expected.clear();
		expected.add("deployed");
		expected.add("staging");
		expected.add("staged");
		assertPath(lifecycle, "deployed", "staged", expected);

		// Staged -> Undeployed
		expected.clear();
		expected.add("staged");
		expected.add("starting");
		expected.add("started");
		expected.add("stopping");
		expected.add("deployed");
		expected.add("undeploying");
		expected.add("undeployed");
		assertPath(lifecycle, "staged", "undeployed", expected);

		// Undeployed -> Started
		expected.clear();
		expected.add("undeployed");
		expected.add("deploying");
		expected.add("deployed");
		expected.add("staging");
		expected.add("staged");
		expected.add("starting");
		expected.add("started");
		assertPath(lifecycle, "undeployed", "started", expected);
	}

}
