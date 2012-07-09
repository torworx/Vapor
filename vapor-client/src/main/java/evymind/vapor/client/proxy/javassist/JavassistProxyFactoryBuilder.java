package evymind.vapor.client.proxy.javassist;

import evymind.vapor.client.proxy.ProxyFactory;
import evymind.vapor.client.proxy.ProxyFactoryBuilder;

public class JavassistProxyFactoryBuilder implements ProxyFactoryBuilder<JavassistProxyFactoryBuilder> {
	
	private Class<?> superclass;
	private Class<?>[] interfaces;

	@Override
	public JavassistProxyFactoryBuilder superclass(Class<?> superclass) {
		this.superclass = superclass;
		return this;
	}

	@Override
	public JavassistProxyFactoryBuilder interfaces(Class<?>... interfaces) {
		this.interfaces = interfaces;
		return this;
	}

	@Override
	public ProxyFactory build() {
		return new JavassistProxyFactory(JavassistProxyHandler.getProxyFactory(superclass, interfaces),
				superclass, interfaces);
	}

}
