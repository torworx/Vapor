package evymind.vapor.core.reflect.asm;

import java.lang.reflect.Method;

import evyframework.reflectasm.MethodAccess;
import evymind.vapor.core.reflect.ClassInvoker;
import evymind.vapor.core.reflect.MethodFilter;
import evymind.vapor.core.reflect.MethodInvoker;
import evymind.vapor.core.reflect.ParameterResolverFactory;
import evymind.vapor.core.reflect.ParameterResolverUtils;

public class ASMClassInvoker extends ClassInvoker {
	
	private final MethodAccess access;

	public ASMClassInvoker(Class<?> targetType, ParameterResolverFactory parameterResolverFactory) {
		super(targetType, parameterResolverFactory);
		access = MethodAccess.get(targetType);
	}

	public ASMClassInvoker(Class<?> targetType, MethodFilter methodFilter, ParameterResolverFactory parameterResolverFactory) {
		super(targetType, methodFilter, parameterResolverFactory);
		access = MethodAccess.get(targetType);
	}

	@Override
	protected MethodInvoker createMethodInvoker(Method method, ParameterResolverFactory parameterResolverFactory) {
		return new ASMMethodInvoker(access, method, ParameterResolverUtils.getResolvers(method, parameterResolverFactory));
	}

}
