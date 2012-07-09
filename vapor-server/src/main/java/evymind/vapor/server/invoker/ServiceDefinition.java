package evymind.vapor.server.invoker;

public class ServiceDefinition {

	private Class<?> serviceInterface;
	private Class<?> serviceImplementation;
	private ServiceScope scope;
	private Object serviceInstance;

	public ServiceDefinition() {
	}

	public <T> ServiceDefinition(Class<T> serviceInterface, Class<? extends T> serviceImplementation,
			ServiceScope scope) {
		this.serviceInterface = serviceInterface;
		this.serviceImplementation = serviceImplementation;
		this.scope = scope;
	}

	public <T> ServiceDefinition(Class<T> serviceInterface, T serviceInstance) {
		this.serviceInterface = serviceInterface;
		this.serviceInstance = serviceInstance;
	}

	@SuppressWarnings("unchecked")
	public <T> Class<T> getServiceInterface() {
		return (Class<T>) serviceInterface;
	}
	
	public void setServiceInterface(Class<?> serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	@SuppressWarnings("unchecked")
	public <T> Class<T> getServiceImplementation() {
		return (Class<T>) serviceImplementation;
	}
	
	public void setServiceImplementation(Class<?> serviceImplementation) {
		this.serviceImplementation = serviceImplementation;
	}

	public ServiceScope getScope() {
		return scope == null ? ServiceScope.singleton : scope;
	}
	
	public void setScope(ServiceScope scope) {
		this.scope = scope;
	}

	@SuppressWarnings("unchecked")
	public <T> T getServiceInstance() {
		return (T) serviceInstance;
	}
	
	public void setServiceInstance(Object serviceInstance) {
		this.serviceInstance = serviceInstance;
	}
}
