package evymind.vapor.deploy.bindings;

import evymind.vapor.deploy.App;
import evymind.vapor.deploy.AppLifecycle;
import evymind.vapor.deploy.graph.Node;
import evymind.vapor.server.handler.ContextHandler;

public class StandardStarter implements AppLifecycle.Binding {
	
	public String[] getBindingTargets() {
		return new String[] { "starting" };
	}

	public void processBinding(Node node, App app) throws Exception {
		ContextHandler handler = app.getContextHandler();
		if (!handler.isStarted()) {
			handler.start();
		}
	}
}
