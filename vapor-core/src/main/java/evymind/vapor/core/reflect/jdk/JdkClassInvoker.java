package evymind.vapor.core.reflect.jdk;

import java.lang.reflect.Method;

import evymind.vapor.core.reflect.ClassInvoker;
import evymind.vapor.core.reflect.MethodFilter;
import evymind.vapor.core.reflect.MethodInvoker;
import evymind.vapor.core.reflect.ParameterResolverFactory;
import evymind.vapor.core.reflect.ParameterResolverUtils;

public class JdkClassInvoker extends ClassInvoker {
	
	public JdkClassInvoker(Class<?> targetType, ParameterResolverFactory parameterResolverFactory) {
		super(targetType, parameterResolverFactory);
	}

	public JdkClassInvoker(Class<?> targetType, MethodFilter methodFilter, ParameterResolverFactory parameterResolverFactory) {
		super(targetType, methodFilter, parameterResolverFactory);
	}

	@Override
	protected MethodInvoker createMethodInvoker(Method method, ParameterResolverFactory parameterResolverFactory) {
		return new JdkMethodInvoker(method, ParameterResolverUtils.getResolvers(method, parameterResolverFactory));
	}

}
