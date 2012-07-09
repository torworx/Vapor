package evymind.vapor.core.reflect;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Abstract Factory that provides access to all ParameterResolverFactory
 * implementations.
 * <p/>
 * Application developers may provide custom ParameterResolverFactory
 * implementations using the ServiceLoader mechanism. To do so, place a file
 * called
 * <code>org.axonframework.common.annotation.ParameterResolverFactory</code> in
 * the <code>META-INF/services</code> folder. In this file, place the fully
 * qualified class names of all available implementations.
 * <p/>
 * The factory implementations must be public, non-abstract, have a default
 * public constructor and extend the ParameterResolverFactory class.
 * 
 * @author Allard Buijze
 * @see ServiceLoader
 * @see ServiceLoader#load(Class)
 * @since 2.0
 */
@SuppressWarnings("rawtypes")
public abstract class ParameterResolverFactory {

	private static final ServiceLoader<ParameterResolverFactory> FACTORY_LOADER = ServiceLoader
			.load(ParameterResolverFactory.class);

	public ParameterResolver[] findResolvers(Annotation[] memberAnnotations, Class<?>[] parameterTypes,
			Annotation[][] parameterAnnotations) {
		int parameters = parameterTypes.length;
		ParameterResolver[] parameterValueResolvers = new ParameterResolver[parameters];
		for (int i = 0; i < parameters; i++) {
			parameterValueResolvers[i] = findParameterResolver(memberAnnotations, parameterTypes[i],
					parameterAnnotations[i]);
		}
		return parameterValueResolvers;
	}

	/**
	 * Iterates over all known ParameterResolverFactory implementations to
	 * create a ParameterResolver instance for the given parameters. The
	 * ParameterResolverFactories invoked in the order they are found on the
	 * classpath. The first to provide a suitable resolver will be used. The
	 * DefaultParameterResolverFactory is always the last one to be inspected.
	 * 
	 * @param memberAnnotations
	 *            annotations placed on the member (e.g. method)
	 * @param parameterType
	 *            the parameter type to find a resolver for
	 * @param parameterAnnotations
	 *            annotations places on the parameter
	 * @return a suitable ParameterResolver, or <code>null</code> if none is
	 *         found
	 */
	public ParameterResolver findParameterResolver(Annotation[] memberAnnotations, Class<?> parameterType,
			Annotation[] parameterAnnotations) {
		ParameterResolver resolver = null;
		Iterator<ParameterResolverFactory> factories = FACTORY_LOADER.iterator();
		while (resolver == null && factories.hasNext()) {
			resolver = factories.next().createInstance(memberAnnotations, parameterType, parameterAnnotations);
		}
		if (resolver == null) {
			resolver = createInstance(memberAnnotations, parameterType, parameterAnnotations);
		}
		return resolver;
	}

	/**
	 * If available, creates a ParameterResolver instance that can provide a
	 * parameter of type <code>parameterType</code> for a given message.
	 * <p/>
	 * If the ParameterResolverFactory cannot provide a suitable
	 * ParameterResolver, returns <code>null</code>.
	 * 
	 * @param memberAnnotations
	 *            annotations placed on the member (e.g. method)
	 * @param parameterType
	 *            the parameter type to find a resolver for
	 * @param parameterAnnotations
	 *            annotations places on the parameter
	 * @return a suitable ParameterResolver, or <code>null</code> if none is
	 *         found
	 */
	protected abstract ParameterResolver createInstance(Annotation[] memberAnnotations, Class<?> parameterType,
			Annotation[] parameterAnnotations);
}
