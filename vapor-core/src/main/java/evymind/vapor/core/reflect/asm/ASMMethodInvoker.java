package evymind.vapor.core.reflect.asm;

import java.lang.reflect.Method;

import evyframework.reflectasm.MethodAccess;
import evymind.vapor.core.reflect.AbstractMethodInvoker;
import evymind.vapor.core.reflect.ParameterResolver;

public class ASMMethodInvoker extends AbstractMethodInvoker {

	private final MethodAccess access;
	private final int index;

	public ASMMethodInvoker(MethodAccess access, Method method, ParameterResolver<?>[] parameterValueResolvers) {
		super(method, parameterValueResolvers);
		this.access = access;
		this.index = access.getIndex(method.getName());
	}

	@Override
	protected Object doInvoke(Object target, Object[] args) throws Exception {
		return access.invoke(target, index, args);
	}

}
