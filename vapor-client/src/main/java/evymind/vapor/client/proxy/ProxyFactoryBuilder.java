package evymind.vapor.client.proxy;

public interface ProxyFactoryBuilder<T extends ProxyFactoryBuilder<T>> {
	
	T superclass(Class<?> clazz);
	
	T interfaces(Class<?>... interfaces);
	
	ProxyFactory build();

}
