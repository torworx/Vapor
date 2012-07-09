package evymind.vapor.server;

import java.util.EventListener;

import evymind.vapor.server.invoker.ServiceScope;

public interface ServiceContext {
	
	<T> void addService(Class<T> serviceInterface, T serviceInstance);
	
	<T> void addService(Class<T> serviceInterface, Class<? extends T> serviceImplementation);
	
	<T> void addService(Class<T> serviceInterface, Class<? extends T> serviceImplementation, ServiceScope scope);
	
	void addListener(String className);
	
	<T extends EventListener> void addListener(T listener);
	
	void addListener(Class<? extends EventListener> listenerClass);

}
