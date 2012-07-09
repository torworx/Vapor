package evymind.vapor.client.proxy.utils;

import evymind.vapor.client.proxy.ServiceProxyFactory;
import evymind.vapor.client.proxy.javassist.JavassistServiceProxyFactory;

public class ServiceProxyUtils {
	
	public static ServiceProxyFactory getDefaultServiceProxyFactory() {
		return new JavassistServiceProxyFactory();
	}
}
