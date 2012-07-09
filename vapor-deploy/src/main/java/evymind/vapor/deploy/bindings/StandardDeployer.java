package evymind.vapor.deploy.bindings;

import evymind.vapor.deploy.App;
import evymind.vapor.deploy.AppLifecycle;
import evymind.vapor.deploy.graph.Node;
import evymind.vapor.server.handler.ContextHandler;

public class StandardDeployer implements AppLifecycle.Binding {
	public String[] getBindingTargets() {
		return new String[] { "deploying" };
	}

	public void processBinding(Node node, App app) throws Exception {
		ContextHandler handler = app.getContextHandler();
		if (handler == null) {
			throw new NullPointerException("No Handler created for App: " + app);
		}
		app.getDeploymentManager().getContexts().addHandler(handler);
	}
}
