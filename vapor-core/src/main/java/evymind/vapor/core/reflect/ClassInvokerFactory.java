package evymind.vapor.core.reflect;

public interface ClassInvokerFactory {
	
	ClassInvoker cteateClassInvoker(Class<?> targetType, ParameterResolverFactory parameterResolverFactory);
	
	ClassInvoker cteateClassInvoker(Class<?> targetType, MethodFilter methodFilter, ParameterResolverFactory parameterResolverFactory);

}
