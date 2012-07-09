package evymind.vapor.server.invoker;

import java.lang.reflect.InvocationTargetException;

import evymind.vapor.core.Message;
import evymind.vapor.core.Transport;
import evymind.vapor.core.reflect.ClassInvoker;
import evymind.vapor.core.reflect.MethodInvoker;
import evymind.vapor.server.Request;
import evymind.vapor.server.Response;

public class DefaultServiceInvoker implements ServiceInvoker {

	private final ClassInvoker classInvoker;
	private final Object service;

	public DefaultServiceInvoker(ClassInvoker classInvoker, Object service) {
		this.classInvoker = classInvoker;
		this.service = service;
	}

	public void invoke(String methodName, Request request, Response response, Transport transport)
			throws InvocationTargetException, IllegalAccessException {
		MethodInvoker methodInvoker = classInvoker.findMethodInvoker(methodName);
		Object result = methodInvoker == null ? null : methodInvoker.invoke(service, request);
		Message message = response.getMessage();
		message.initializeResponseMessage(transport, "", classInvoker.getTargetType().getName(), methodName
				+ "Response");
		if (methodInvoker != null) {
			Class<?> returnType = methodInvoker.getMethod().getReturnType();
			if (returnType != null && !Void.TYPE.equals(returnType)) {
				message.write("result", result, returnType);
			}
		}
		message.finalizeMessage();
	}

	public ClassInvoker getClassInvoker() {
		return classInvoker;
	}

	public Object getService() {
		return service;
	}

}
