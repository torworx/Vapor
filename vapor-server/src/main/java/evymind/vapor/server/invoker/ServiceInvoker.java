package evymind.vapor.server.invoker;

import java.lang.reflect.InvocationTargetException;

import evymind.vapor.core.Transport;
import evymind.vapor.server.Request;
import evymind.vapor.server.Response;

public interface ServiceInvoker {

	void invoke(String methodName, Request request, Response response, Transport transport)
			throws InvocationTargetException, IllegalAccessException;

}
