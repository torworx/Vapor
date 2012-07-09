package evymind.vapor.core.reflect.jdk;

import static java.lang.String.format;

import java.lang.reflect.Method;

import evymind.vapor.core.reflect.AbstractMethodInvoker;
import evymind.vapor.core.reflect.ParameterResolver;

@SuppressWarnings("rawtypes")
public class JdkMethodInvoker extends AbstractMethodInvoker {

	JdkMethodInvoker(Method method, ParameterResolver[] parameterValueResolvers) {
		super(method, parameterValueResolvers);
	}

	@Override
	public Object doInvoke(Object target, Object[] args) throws Exception {
		return method.invoke(target, args);
	}

	@Override
	public String toString() {
		return format("Method invoker %s.%s", method.getDeclaringClass().getSimpleName(), method.getName());
	}
}
