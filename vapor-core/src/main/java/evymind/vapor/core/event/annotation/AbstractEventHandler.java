package evymind.vapor.core.event.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import evyframework.common.Assert;

import evymind.vapor.core.event.Event;
import evymind.vapor.core.reflect.ParameterResolver;

@SuppressWarnings("rawtypes")
public abstract class AbstractEventHandler implements Comparable<AbstractEventHandler> {

	private static final EventParameterResolverFactory parameterResolverFactory = new EventParameterResolverFactory();
	
	private final Score score;
	private final Class payloadType;
	private final ParameterResolver[] parameterValueResolvers;

	/**
	 * Initializes the EventHandler to handle events with the given
	 * <code>payloadType</code>, declared in the given
	 * <code>declaringClass</code> and using the given
	 * <code>parameterValueResolvers</code>.
	 * 
	 * @param payloadType
	 *            The type of payload this handlers deals with
	 * @param declaringClass
	 *            The class on which the handler is declared
	 * @param parameterValueResolvers
	 *            The resolvers for each of the handlers' parameters
	 */
	protected AbstractEventHandler(Class<?> payloadType, Class<?> declaringClass,
			ParameterResolver... parameterValueResolvers) {
		score = new Score(payloadType, declaringClass);
		this.payloadType = payloadType;
		this.parameterValueResolvers = Arrays.copyOf(parameterValueResolvers, parameterValueResolvers.length);
	}

	/**
	 * Indicates whether this Handler is suitable for the given
	 * <code>event</code>.
	 * 
	 * @param event
	 *            The event to inspect
	 * @return <code>true</code> if this handler can handle the event,
	 *         otherwise <code>false</code>.
	 */
	public boolean matches(Event event) {
		Assert.notNull(event, "Event may not be null");
		for (ParameterResolver parameterResolver : parameterValueResolvers) {
			if (!parameterResolver.matches(event)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Invokes this handler for the given <code>target</code> instance, using
	 * the given <code>event</code> as source object to provide parameter
	 * values.
	 * 
	 * @param target
	 *            The target instance to invoke the Handler on.
	 * @param event
	 *            The event providing parameter values
	 * @return The result of the handler invocation
	 * 
	 * @throws InvocationTargetException
	 *             when the handler throws a checked exception
	 * @throws IllegalAccessException
	 *             if the SecurityManager refuses the handler invocation
	 */
	public abstract Object invoke(Object target, Event event) throws InvocationTargetException,
			IllegalAccessException;

	/**
	 * Returns the type of payload this handler expects.
	 * 
	 * @return the type of payload this handler expects
	 */
	public Class getPayloadType() {
		return payloadType;
	}

	@Override
	public int compareTo(AbstractEventHandler o) {
		return score.compareTo(o.score);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof AbstractEventHandler) && ((AbstractEventHandler) obj).score.equals(score);
	}

	@Override
	public int hashCode() {
		return score.hashCode();
	}

	/**
	 * Finds ParameterResolvers for the given Member details. The returning
	 * array contains as many elements as the given <code>parameterTypes</code>,
	 * where each ParameterResolver corresponds with the parameter type at the
	 * same location.
	 * 
	 * @param memberAnnotations
	 *            The annotations on the member (e.g. method)
	 * @param parameterTypes
	 *            The parameter type of the member
	 * @param parameterAnnotations
	 *            The annotations on each of the parameters
	 * @return the parameter resolvers for the given Member details
	 * 
	 * @see java.lang.reflect.Method
	 * @see java.lang.reflect.Constructor
	 */
	protected static ParameterResolver[] findResolvers(Annotation[] memberAnnotations, Class<?>[] parameterTypes,
			Annotation[][] parameterAnnotations) {
		int parameters = parameterTypes.length;
		ParameterResolver[] parameterValueResolvers = new ParameterResolver[parameters];
		for (int i = 0; i < parameters; i++) {
			parameterValueResolvers[i] = parameterResolverFactory.findParameterResolver(memberAnnotations,
					parameterTypes[i], parameterAnnotations[i]);
		}
		if (parameterValueResolvers[0] == null) {
			parameterValueResolvers[0] = parameterResolverFactory.createPayloadResolver(parameterTypes[0]);
		}

		return parameterValueResolvers;
	}

	/**
	 * Returns the parameter resolvers provided at construction time.
	 * 
	 * @return the parameter resolvers provided at construction time
	 */
	protected ParameterResolver[] getParameterValueResolvers() {
		return parameterValueResolvers;
	}

	private static final class Score implements Comparable<Score> {

		private final int declarationDepth;
		private final int payloadDepth;
		private final String payloadName;

		private Score(Class payloadType, Class<?> declaringClass) {
			declarationDepth = superClassCount(declaringClass, 0);
			payloadDepth = superClassCount(payloadType, Integer.MAX_VALUE);
			payloadName = payloadType.getName();
		}

		private int superClassCount(Class<?> declaringClass, int interfaceScore) {
			if (declaringClass.isInterface()) {
				return interfaceScore;
			}
			int superClasses = 0;

			while (declaringClass != null) {
				superClasses++;
				declaringClass = declaringClass.getSuperclass();
			}
			return superClasses;
		}

		@Override
		public int compareTo(Score o) {
			if (declarationDepth != o.declarationDepth) {
				return o.declarationDepth - declarationDepth;
			} else if (payloadDepth != o.payloadDepth) {
				return o.payloadDepth - payloadDepth;
			} else {
				return payloadName.compareTo(o.payloadName);
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			Score score = (Score) o;

			if (declarationDepth != score.declarationDepth) {
				return false;
			}
			if (payloadDepth != score.payloadDepth) {
				return false;
			}
			if (!payloadName.equals(score.payloadName)) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = declarationDepth;
			result = 31 * result + payloadDepth;
			result = 31 * result + payloadName.hashCode();
			return result;
		}
	}
}
