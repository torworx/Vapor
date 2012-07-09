package evymind.vapor.core.reflect;

import static java.lang.String.format;

import java.lang.reflect.Method;

import evyframework.common.ReflectionUtils;

public class ParameterResolverUtils {
	
	private static void validate(Method method, ParameterResolver<?>[] parameterResolvers) {
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			if (parameterResolvers[i] == null) {
				throw new UnsupportedMethodException(format(
						"On method %s, parameter %s is invalid. It is not of any format supported by a provided"
								+ "ParameterValueResolver.", method.toGenericString(), i + 1), method);
			}
		}
//
//		/*
//		 * special case: methods with equal signature on EventListener must be rejected, because it interferes with the
//		 * Proxy mechanism
//		 */
//		if (method.getName().equals("handle") && Arrays.equals(method.getParameterTypes(), new Class[] { Event.class })) {
//			throw new UnsupportedMethodException(String.format(
//					"Event Handling class %s contains method %s that has a naming conflict with a "
//							+ "method on the EventHandler interface. Please rename the method.", method
//							.getDeclaringClass().getSimpleName(), method.getName()), method);
//		}
	}
	
	public static ParameterResolver<?>[] getResolvers(Method method, ParameterResolverFactory parameterResolverFactory) {
		ParameterResolver<?>[] resolvers = parameterResolverFactory.findResolvers(method.getAnnotations(),
				method.getParameterTypes(), method.getParameterAnnotations());
		// Class<?> firstParameter = method.getParameterTypes()[0];
		ReflectionUtils.makeAccessible(method);
		validate(method, resolvers);
		return resolvers;
	}
}
