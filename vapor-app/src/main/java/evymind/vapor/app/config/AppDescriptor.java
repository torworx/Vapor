package evymind.vapor.app.config;

import java.util.EventListener;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.app.AppContext;
import evymind.vapor.server.invoker.ServiceDefinition;

public class AppDescriptor implements Configurable<AppContext> {
	
	private static final Logger log = LoggerFactory.getLogger(AppDescriptor.class);
	
	private List<ServiceDescriptor> services;
	
	private List<Class<?>> listeners;

	@Override
	public void configure(AppContext context) {
		// services configure
		if (services != null && !services.isEmpty()) {
			ServiceDefinition[] definitions = new ServiceDefinition[services.size()];
			int i = 0;
			for (ServiceDescriptor descriptor : services) {
				definitions[i++] = descriptor.getServiceDefinition();
			}
			context.getServiceHandler().setServices(definitions);
		}
		
		// listeners configure
		if (listeners != null && !listeners.isEmpty()) {
			for (Class<?> listenerClass : listeners) {
				if (!EventListener.class.isAssignableFrom(listenerClass)) {
					log.warn("Listener [" + listenerClass + "] is not a java.util.EventHandler, Ignored.");
					continue;
				}
				try {
					EventListener listener = (EventListener) listenerClass.newInstance();
					context.addListener(listener);
				} catch (Exception e) {
					log.warn("Listener initiate failure for " + listenerClass + ", Ignored.", e);
				} 
			}
		}
		
	}

	public List<ServiceDescriptor> getServices() {
		return services;
	}

	public void setServices(List<ServiceDescriptor> services) {
		this.services = services;
	}

	public List<Class<?>> getListeners() {
		return listeners;
	}

	public void setListeners(List<Class<?>> listeners) {
		this.listeners = listeners;
	}
	
}
