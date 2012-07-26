package evymind.vapor.server.invoker;

import evyframework.common.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ServiceDefinition {

	private Class<?> serviceInterface;
	private Class<?> serviceImplementation;
	private ServiceScope scope;
	private Object serviceInstance;
    private final List<String> aliasNames = new ArrayList<String>();

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

    public void registerServiceName(String serviceName) {
        aliasNames.add(resolveServiceName(serviceName));
    }

    public void unregisterServiceName(String serviceName) {
        aliasNames.remove(resolveServiceName(serviceName));
    }

    protected String resolveServiceName(String serviceName) {
        Assert.hasLength(serviceName, "'serviceName' must not be blank");
        String resolvedServiceName = serviceName;
        if (!resolvedServiceName.startsWith("_")) {
            resolvedServiceName = "_" + resolvedServiceName;
        }
        if (!(resolvedServiceName.endsWith("._tcp.") || (resolvedServiceName.endsWith("._udp.")))) {
            if (!resolvedServiceName.endsWith("_vapor.")) {
                resolvedServiceName += "_vapor.";
            }
            resolvedServiceName += "_tcp.";
        }
        return resolvedServiceName;
    }

    public List<String> getAliasNames() {
        if (aliasNames.isEmpty()) {
            return Collections.singletonList(resolveServiceName(getServiceInterface().getSimpleName()));
        }
        return Collections.unmodifiableList(aliasNames);
    }
}
