package evymind.vapor.client.proxy.javassist;

import java.lang.reflect.Method;

import org.slf4j.LoggerFactory;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import evymind.vapor.client.proxy.BasicProxyHandler;
import evymind.vapor.client.proxy.ProxyException;
import evymind.vapor.core.Message;
import evymind.vapor.core.TransportChannel;

public class JavassistProxyHandler extends BasicProxyHandler implements MethodHandler {
	
//	private static final Logger log = LoggerFactory.getLogger(JavassistProxyHandler.class);

	private static final MethodFilter FINALIZE_FILTER = new MethodFilter() {
		public boolean isHandled(Method m) {
			// skip finalize methods
			return !(m.getParameterTypes().length == 0 && m.getName().equals("finalize"));
		}
	};

	private boolean constructed = false;

	private JavassistProxyHandler(Class<?>[] interfaces, Message message, TransportChannel transportChannel) {
		super(interfaces, message, transportChannel);
	}

	public static Class<?> getProxyFactory(Class<?> superclass, Class<?>[] interfaces) throws ProxyException {
		try {
			ProxyFactory factory = new ProxyFactory();
			factory.setSuperclass(superclass);
			factory.setInterfaces(interfaces);
			factory.setFilter(FINALIZE_FILTER);
			return factory.createClass();
		} catch (Throwable t) {
			LoggerFactory.getLogger(BasicProxyHandler.class).error("Javassist Enhancement failed: " + interfaces, t);
			throw new ProxyException("Javassist Enhancement failed: " + interfaces, t);
		}
	}

	public static Object getProxy(Class<?> superclass, Class<?>[] interfaces, Class<?> factory, Message message,
			TransportChannel transportChannel) {
		final JavassistProxyHandler handler = new JavassistProxyHandler(interfaces, message, transportChannel);
		final Object proxy;
		try {
			proxy = factory.newInstance();
		} catch (Exception e) {
			throw new ProxyException("Javassist Enhancement failed: " + interfaces, e);
		}

		((ProxyObject) proxy).setHandler(handler);
		handler.constructed = true;
		return proxy;
	}

	@Override
	public Object invoke(Object proxy, Method thisMethod, Method proceed, Object[] args) throws Throwable {
		if (this.constructed) {
			Object result;
			try {
				result = this.invoke(thisMethod, args, proxy);
			} catch (Throwable t) {
				throw t;
			}
			if (result == INVOKE_IMPLEMENTATION) {
				throw new ProxyException("Unproxied method " + thisMethod);
			} else {
				return result;
			}
		} else {
			// while constructor is running
			if (thisMethod.getName().equals("getProxyHandler")) {
				return this;
			} else {
				return proceed.invoke(proxy, args);
			}
		}
	}

}
