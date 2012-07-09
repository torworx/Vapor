package evymind.vapor.core.reflect;

import evyframework.common.ClassUtils;
import evymind.vapor.core.reflect.asm.ASMClassInvoker;
import evymind.vapor.core.reflect.jdk.JdkClassInvoker;

public class DefaultClassInvokerFactory implements ClassInvokerFactory {
	
	/** Whether the ASM library is present on the classpath */
	private static final boolean asmAvailable =
			ClassUtils.isPresent("org.objectweb.asm.ClassWriter", DefaultClassInvokerFactory.class.getClassLoader());

	@Override
	public ClassInvoker cteateClassInvoker(Class<?> targetType, ParameterResolverFactory parameterResolverFactory) {
		return cteateClassInvoker(targetType, null, parameterResolverFactory);
	}

	@Override
	public ClassInvoker cteateClassInvoker(Class<?> targetType, MethodFilter methodFilter, ParameterResolverFactory parameterResolverFactory) {
		if (asmAvailable) {
			return ASMClassInvokerFactory.createASMClassInvoker(targetType, methodFilter, parameterResolverFactory);
		} else {
			return new JdkClassInvoker(targetType, methodFilter, parameterResolverFactory).initialize();
		}
	}

	/**
	 * Inner factory class used to just introduce a ASM dependency
	 * when actually creating a ASM class invoker
	 */
	private static class ASMClassInvokerFactory {
		public static ClassInvoker createASMClassInvoker(Class<?> targetType, MethodFilter methodFilter, ParameterResolverFactory parameterResolverFactory) {
			return new ASMClassInvoker(targetType, methodFilter, parameterResolverFactory).initialize();
		}
	}
}
