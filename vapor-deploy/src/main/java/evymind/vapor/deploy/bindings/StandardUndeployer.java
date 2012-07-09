package evymind.vapor.deploy.bindings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.deploy.App;
import evymind.vapor.deploy.AppLifecycle;
import evymind.vapor.deploy.graph.Node;
import evymind.vapor.server.Handler;
import evymind.vapor.server.handler.ContextHandler;
import evymind.vapor.server.handler.ContextHandlerCollection;
import evymind.vapor.server.handler.HandlerCollection;

public class StandardUndeployer implements AppLifecycle.Binding {
	
	private static final Logger LOG = LoggerFactory.getLogger(StandardUndeployer.class);

	public String[] getBindingTargets() {
		return new String[] { "undeploying" };
	}

	public void processBinding(Node node, App app) throws Exception {
		ContextHandler handler = app.getContextHandler();
		ContextHandlerCollection chcoll = app.getDeploymentManager().getContexts();

		recursiveRemoveContext(chcoll, handler);
	}

	private void recursiveRemoveContext(HandlerCollection coll, ContextHandler context) {
		Handler children[] = coll.getHandlers();
		int originalCount = children.length;

		for (int i = 0, n = children.length; i < n; i++) {
			Handler child = children[i];
			LOG.debug("Child handler {}", child);
			if (child.equals(context)) {
				LOG.debug("Removing handler {}", child);
				coll.removeHandler(child);
				child.destroy();
				if (LOG.isDebugEnabled())
					LOG.debug("After removal: {} (originally {})", coll.getHandlers().length, originalCount);
			} else if (child instanceof HandlerCollection) {
				recursiveRemoveContext((HandlerCollection) child, context);
			}
		}
	}
}
