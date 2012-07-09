package evymind.vapor.deploy.bindings;

import java.util.LinkedList;

import evymind.vapor.deploy.App;
import evymind.vapor.deploy.AppLifecycle;
import evymind.vapor.deploy.graph.Node;

/**
 * Provides a way of forcing the ordered execution of bindings within a declared binding target.
 * 
 */
public class OrderedGroupBinding implements AppLifecycle.Binding {
	private String[] _bindingTargets;

	private LinkedList<AppLifecycle.Binding> _orderedBindings;

	public OrderedGroupBinding(String[] bindingTargets) {
		_bindingTargets = bindingTargets;
	}

	public void addBinding(AppLifecycle.Binding binding) {
		if (_orderedBindings == null) {
			_orderedBindings = new LinkedList<AppLifecycle.Binding>();
		}

		_orderedBindings.add(binding);
	}

	public void addBindings(AppLifecycle.Binding[] bindings) {
		if (_orderedBindings == null) {
			_orderedBindings = new LinkedList<AppLifecycle.Binding>();
		}

		for (AppLifecycle.Binding binding : bindings) {
			_orderedBindings.add(binding);
		}
	}

	public String[] getBindingTargets() {
		return _bindingTargets;
	}

	public void processBinding(Node node, App app) throws Exception {
		for (AppLifecycle.Binding binding : _orderedBindings) {
			binding.processBinding(node, app);
		}
	}
}
