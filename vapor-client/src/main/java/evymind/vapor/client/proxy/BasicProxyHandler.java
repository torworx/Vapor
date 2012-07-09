package evymind.vapor.client.proxy;

import java.lang.reflect.Method;
import java.util.Set;

import com.google.common.collect.Sets;

import evyframework.common.ReflectionUtils;
import evymind.vapor.client.DefaultServiceProxyInvoker;
import evymind.vapor.client.Parameters;
import evymind.vapor.client.ServiceProxyInvoker;
import evymind.vapor.core.Message;
import evymind.vapor.core.TransportChannel;
import evymind.vapor.core.utils.MarkerObject;

public class BasicProxyHandler {
	
	protected static final Object INVOKE_IMPLEMENTATION = new MarkerObject("INVOKE_IMPLEMENTATION");
	
	protected ServiceProxyInvoker invoker;
	
	protected Class<?>[] interfaces;
	protected Message message;
	protected TransportChannel transportChannel;
	
	protected Set<String> interfaceMethodNames = Sets.newHashSet();
	
	protected BasicProxyHandler(Class<?>[] interfaces, Message message, TransportChannel transportChannel) {
		this.interfaces = interfaces;
		this.message = message;
		this.transportChannel = transportChannel;
		
		this.invoker = new DefaultServiceProxyInvoker(message, transportChannel);
		
		initInterfacesMethods();
	}
	
	protected void initInterfacesMethods() {
		for (Class<?> intf : interfaces) {
			for (Method method : ReflectionUtils.getAllDeclaredMethods(intf)) {
				interfaceMethodNames.add(method.getName());
			}
		}
	}
	
	protected final Object invoke(Method method, Object[] args, Object proxy) throws Throwable {
		if (interfaceMethodNames.contains(method.getName())) {
			return invoker.invoke(method.getDeclaringClass().getName(), method.getName(), method.getReturnType(), new Parameters(args));
//			message.initializeRequestMessage(transportChannel, "", method.getDeclaringClass().getName(), method.getName());
//			Class<?>[] parameterTypes = method.getParameterTypes();
//			int length = Math.min(args.length, parameterTypes.length);
//			for (int i = 0; i < length; i++) {
//				// TODO support parameter name
//				message.write("", args[i], parameterTypes[i]);
//			}
//			message.finalizeMessage();
//			transportChannel.dispatch(message);
//			Class<?> returnType = method.getReturnType();
//			if (returnType != null && !Void.TYPE.equals(returnType)) {
//				return message.read("result", returnType);
//			} else {
//				return null;
//			}
		}
		return INVOKE_IMPLEMENTATION;
	}

}
