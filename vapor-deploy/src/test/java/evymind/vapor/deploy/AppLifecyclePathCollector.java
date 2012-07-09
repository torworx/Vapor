package evymind.vapor.deploy;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import evymind.vapor.deploy.graph.Node;

/**
 * Binds to all lifecycle nodes, and tracks the order of the lifecycle nodes for
 * testing purposes.
 */
public class AppLifecyclePathCollector implements AppLifecycle.Binding {
	
	List<Node> actualOrder = new ArrayList<Node>();

	public void clear() {
		actualOrder.clear();
	}

	public List<Node> getCapturedPath() {
		return actualOrder;
	}

	public String[] getBindingTargets() {
		return new String[] { "*" };
	}

	public void processBinding(Node node, App app) throws Exception {
		actualOrder.add(node);
	}

	public void assertExpected(String msg, List<String> expectedOrder) {
		if (expectedOrder.size() != actualOrder.size()) {
			System.out.println("/* Expected Path */");
			for (String path : expectedOrder) {
				System.out.println(path);
			}
			System.out.println("/* Actual Path */");
			for (Node path : actualOrder) {
				System.out.println(path.getName());
			}

			Assert.assertEquals(msg + " / count", expectedOrder.size(), actualOrder.size());
		}

		for (int i = 0, n = expectedOrder.size(); i < n; i++) {
			Assert.assertEquals(msg + "[" + i + "]", expectedOrder.get(i), actualOrder.get(i).getName());
		}
	}
}
