package evymind.vapor.deploy.bindings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.deploy.App;
import evymind.vapor.deploy.AppLifecycle;
import evymind.vapor.deploy.graph.Node;

public class DebugBinding implements AppLifecycle.Binding {
	private static final Logger LOG = LoggerFactory.getLogger(DebugBinding.class);

	final String[] _targets;

	public DebugBinding(String target) {
		_targets = new String[] { target };
	}

	public DebugBinding(final String... targets) {
		_targets = targets;
	}

	public String[] getBindingTargets() {
		return _targets;
	}

	public void processBinding(Node node, App app) throws Exception {
		LOG.info("processBinding {} {}", node, app.getContextHandler());
	}
}
