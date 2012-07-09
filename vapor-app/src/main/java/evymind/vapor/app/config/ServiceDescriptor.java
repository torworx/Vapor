package evymind.vapor.app.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import evymind.vapor.core.utils.Loader;
import evymind.vapor.server.invoker.ServiceDefinition;
import evymind.vapor.server.invoker.ServiceScope;

public class ServiceDescriptor {
	
	private final ServiceDefinition serviceDefinition = new ServiceDefinition();

	public ServiceDefinition getServiceDefinition() {
		return serviceDefinition;
	}

	public String getServiceInterface() {
		return serviceDefinition.getServiceInterface().getName();
	}

	@JsonProperty("interface")
	public void setServiceInterface(String serviceInterface) throws ClassNotFoundException {
		serviceDefinition.setServiceInterface(Loader.loadClass(this.getClass(), serviceInterface));
	}

	public String getServiceClass() {
		if (serviceDefinition.getServiceImplementation() == null) 
			return null;
		return serviceDefinition.getServiceImplementation().getName();
	}

	@JsonProperty("class")
	public void setServiceClass(String serviceClass) throws ClassNotFoundException {
		serviceDefinition.setServiceImplementation(Loader.loadClass(this.getClass(), serviceClass));
	}

	public ServiceScope getScope() {
		return serviceDefinition.getScope();
	}

	@JsonProperty("scope")
	public void setScope(ServiceScope scope) {
		serviceDefinition.setScope(scope);
	}
	

}
