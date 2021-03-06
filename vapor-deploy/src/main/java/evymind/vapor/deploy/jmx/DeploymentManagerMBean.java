package evymind.vapor.deploy.jmx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import evymind.vapor.deploy.App;
import evymind.vapor.deploy.AppProvider;
import evymind.vapor.deploy.DeploymentManager;
import evymind.vapor.deploy.graph.Node;
import evymind.vapor.jmx.ObjectMBean;
import evymind.vapor.server.handler.ContextHandler;

public class DeploymentManagerMBean extends ObjectMBean {
	
	private final DeploymentManager _manager;

	public DeploymentManagerMBean(Object managedObject) {
		super(managedObject);
		_manager = (DeploymentManager) managedObject;
	}

	public Collection<String> getNodes() {
		List<String> nodes = new ArrayList<String>();
		for (Node node : _manager.getNodes())
			nodes.add(node.getName());
		return nodes;
	}

	public Collection<String> getApps() {
		List<String> apps = new ArrayList<String>();
		for (App app : _manager.getApps())
			apps.add(app.getOriginId());
		return apps;
	}

	public Collection<String> getApps(String nodeName) {
		List<String> apps = new ArrayList<String>();
		for (App app : _manager.getApps(nodeName))
			apps.add(app.getOriginId());
		return apps;
	}

	public Collection<ContextHandler> getContexts() throws Exception {
		List<ContextHandler> apps = new ArrayList<ContextHandler>();
		for (App app : _manager.getApps())
			apps.add(app.getContextHandler());
		return apps;
	}

	public Collection<AppProvider> getAppProviders() {
		return _manager.getAppProviders();
	}

	public void requestAppGoal(String appId, String nodeName) {
		_manager.requestAppGoal(appId, nodeName);
	}
}
