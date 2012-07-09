package evymind.vapor.deploy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.deploy.graph.Graph;
import evymind.vapor.deploy.graph.Node;

/**
 * The lifecycle of an App in the {@link DeploymentManager}.
 * 
 * Setups a the default {@link Graph}, and manages the bindings to the life cycle via the {@link AppLifecycle.Binding}
 * annotation.
 * <p>
 * <img src="doc-files/AppLifecycle.png">
 */
public class AppLifecycle extends Graph {
	private static final Logger LOG = LoggerFactory.getLogger(AppLifecycle.class);

	private static final String ALL_NODES = "*";

	public static interface Binding {
		/**
		 * Get a list of targets that this implementation should bind to.
		 * 
		 * @return the array of String node names to bind to. (use <code>"*"</code> to bind to all known node names)
		 */
		String[] getBindingTargets();

		/**
		 * Event called to process a {@link AppLifecycle} binding.
		 * 
		 * @param node
		 *            the node being processed
		 * @param app
		 *            the app being processed
		 * @throws Exception
		 *             if any problem severe enough to halt the AppLifecycle processing
		 */
		void processBinding(Node node, App app) throws Exception;
	}

	// Well known existing lifecycle Nodes
	public static final String UNDEPLOYED = "undeployed";
	public static final String DEPLOYING = "deploying";
	public static final String DEPLOYED = "deployed";
	public static final String STARTING = "starting";
	public static final String STARTED = "started";
	public static final String STOPPING = "stopping";
	public static final String UNDEPLOYING = "undeploying";

	private Map<String, List<Binding>> lifecyclebindings = new HashMap<String, List<Binding>>();

	public AppLifecycle() {
		// Define Default Graph

		// undeployed -> deployed
		addEdge(UNDEPLOYED, DEPLOYING);
		addEdge(DEPLOYING, DEPLOYED);

		// deployed -> started
		addEdge(DEPLOYED, STARTING);
		addEdge(STARTING, STARTED);

		// started -> deployed
		addEdge(STARTED, STOPPING);
		addEdge(STOPPING, DEPLOYED);

		// deployed -> undeployed
		addEdge(DEPLOYED, UNDEPLOYING);
		addEdge(UNDEPLOYING, UNDEPLOYED);
	}

	public void addBinding(AppLifecycle.Binding binding) {
		for (String nodeName : binding.getBindingTargets()) {
			List<Binding> bindings = lifecyclebindings.get(nodeName);
			if (bindings == null) {
				bindings = new ArrayList<Binding>();
			}
			bindings.add(binding);

			lifecyclebindings.put(nodeName, bindings);
		}
	}

	public void removeBinding(AppLifecycle.Binding binding) {
		for (String nodeName : binding.getBindingTargets()) {
			List<Binding> bindings = lifecyclebindings.get(nodeName);
			if (bindings != null)
				bindings.remove(binding);
		}
	}

	/**
	 * Get all {@link Node} bound objects.
	 * 
	 * @return Set of Object(s) for all lifecycle bindings. never null.
	 */
	public Set<AppLifecycle.Binding> getBindings() {
		Set<Binding> boundset = new HashSet<Binding>();

		for (List<Binding> bindings : lifecyclebindings.values()) {
			boundset.addAll(bindings);
		}

		return boundset;
	}

	/**
	 * Get all objects bound to a specific {@link Node}
	 * 
	 * @return Set of Object(s) for specific lifecycle bindings. never null.
	 */
	public Set<AppLifecycle.Binding> getBindings(Node node) {
		return getBindings(node.getName());
	}

	/**
	 * Get all objects bound to a specific {@link Node}
	 * 
	 * @return Set of Object(s) for specific lifecycle bindings. never null.
	 */
	public Set<AppLifecycle.Binding> getBindings(String nodeName) {
		Set<Binding> boundset = new HashSet<Binding>();

		// Specific node binding
		List<Binding> bindings = lifecyclebindings.get(nodeName);
		if (bindings != null) {
			boundset.addAll(bindings);
		}

		// Special 'all nodes' binding
		bindings = lifecyclebindings.get(ALL_NODES);
		if (bindings != null) {
			boundset.addAll(bindings);
		}

		return boundset;
	}

	public void runBindings(Node node, App app, DeploymentManager deploymentManager) throws Throwable {
		for (Binding binding : getBindings(node)) {
			if (LOG.isDebugEnabled())
				LOG.debug("Calling " + binding.getClass().getName() + " for " + app);
			binding.processBinding(node, app);
		}
	}
}
