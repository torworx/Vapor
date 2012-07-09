package evymind.vapor.core.reflect;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import evyframework.common.ReflectionUtils;

public abstract class AbstractClassInspector<T extends AbstractClassInspector<T>> {
	
	private static final ObjectMethodsExcludeFilter OBJECT_METHODS_EXCLUDE_FILTER = new ObjectMethodsExcludeFilter();
	
	protected final Class<?> targetType;
	protected final Method[] targetMethods;
	protected final MethodFilter methodFilter;
	protected final ParameterResolverFactory parameterResolverFactory;
	protected final SortedMap<String, MethodInvoker> invokers = new TreeMap<String, MethodInvoker>();
	
	protected AbstractClassInspector(Class<?> targetType, ParameterResolverFactory parameterResolverFactory) {
		this(targetType, null, parameterResolverFactory);
	}

	protected AbstractClassInspector(Class<?> targetType, MethodFilter methodFilter, ParameterResolverFactory parameterResolverFactory) {
		this.targetType = targetType;
		this.targetMethods = getMethods(targetType);
		this.methodFilter = methodFilter == null ? OBJECT_METHODS_EXCLUDE_FILTER : methodFilter;
		this.parameterResolverFactory = parameterResolverFactory;
	}
	
	@SuppressWarnings("unchecked")
	protected T initialize() {
		invokers.clear();
		MethodFilter filter = getMethodFilter();
		Method[] methods = getTargetMethods();
		for (Method method : methods) {
			if (filter != null && filter.isHandled(method)) {
				MethodInvoker methodMethodInvoker = createMethodInvoker(method, parameterResolverFactory);
				if (invokers.containsKey(method.getName())) {
					throw new UnsupportedMethodException(String.format(
							"The class %s contains two methods with same name: %s", method.getDeclaringClass()
									.getSimpleName(), methodMethodInvoker.getMethodName()), method);
				}
				invokers.put(method.getName(), methodMethodInvoker);
			}
		}
		return (T) this;
	}
	
	protected Method[] getMethods(Class<?> cls) {
		return ReflectionUtils.getUniqueDeclaredMethods(cls);
	}
	
	protected Method[] getTargetMethods() {
		return targetMethods;
	}
	
	protected MethodFilter getMethodFilter() {
		return methodFilter;
	}
	
	protected abstract MethodInvoker createMethodInvoker(Method method, ParameterResolverFactory parameterResolverFactory);

	/**
	 * Returns the handler method that handles objects of the given
	 * <code>parameterType</code>. Returns <code>null</code> is no such method
	 * is found.
	 * 
	 * @param event
	 *            The event to find a handler for
	 * @return the handler method for the given parameterType
	 */
	public MethodInvoker findMethodInvoker(final String methodName) {
		return invokers.get(methodName);
	}

	/**
	 * Returns the list of handlers found on target type.
	 * 
	 * @return the list of handlers found on target type
	 */
	public Map<String, MethodInvoker> getInvokers() {
		return new HashMap<String, MethodInvoker>(invokers);
	}

	/**
	 * Returns the targetType on which handler methods are invoked.
	 * 
	 * @return the targetType on which handler methods are invoked
	 */
	public Class<?> getTargetType() {
		return targetType;
	}
}
