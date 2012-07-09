package evymind.vapor.client;

public interface ServiceProxyInvoker {
	
	void invoke(String interfaceName, String messageName);
	
	void invoke(String interfaceName, String messageName, Parameters parameters);
	
	<T> T invoke(String interfaceName, String messageName, Class<T> returnType);
	
	<T> T invoke(String interfaceName, String messageName, Class<T> returnType, Parameters parameters);
}
