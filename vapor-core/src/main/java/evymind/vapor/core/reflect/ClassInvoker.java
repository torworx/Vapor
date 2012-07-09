package evymind.vapor.core.reflect;

import java.lang.reflect.InvocationTargetException;

public abstract class ClassInvoker extends AbstractClassInspector<ClassInvoker> {

	protected ClassInvoker(Class<?> targetType, ParameterResolverFactory parameterResolverFactory) {
		super(targetType, parameterResolverFactory);
	}

	protected ClassInvoker(Class<?> targetType, MethodFilter methodFilter, ParameterResolverFactory parameterResolverFactory) {
		super(targetType, methodFilter, parameterResolverFactory);
	}

	public Object invokeMethod(Object target, String methodName, Object parameter) throws InvocationTargetException, IllegalAccessException {
        return invokeMethod(target, methodName, parameter, NullReturningCallback.INSTANCE);
    }

    public Object invokeMethod(Object target, String methodName, Object parameter, NoMethodFoundCallback onNoMethodFound)
            throws InvocationTargetException, IllegalAccessException {
        MethodInvoker m = findMethodInvoker(methodName);
        if (m == null) {
            // event listener doesn't support this type of parameter
            return onNoMethodFound.onNoMethodFound(methodName, parameter);
        }

        return m.invoke(target, parameter);
    }
    
	/**
     * Callback used in cases where the handler did not find a suitable method to invoke.
     */
    public interface NoMethodFoundCallback {

        /**
         * Indicates what needs to happen when no handler is found for a given parameter. The default behavior is to
         * return <code>null</code>.
         *
         * @param parameter The parameter for which no handler could be found
         * @return the value to return when no handler method is found. Defaults to <code>null</code>.
         */
        Object onNoMethodFound(String methodName, Object parameter);
    }

    private static class NullReturningCallback implements NoMethodFoundCallback {

        private static final NullReturningCallback INSTANCE = new NullReturningCallback();

        @Override
        public Object onNoMethodFound(String methodName, Object parameter) {
            return null;
        }
    }
}
