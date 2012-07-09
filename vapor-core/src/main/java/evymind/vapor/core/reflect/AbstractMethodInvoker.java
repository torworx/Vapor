package evymind.vapor.core.reflect;

import java.lang.reflect.Method;

import evyframework.common.Assert;

import evymind.vapor.core.event.annotation.AbstractEventHandler;

public abstract class AbstractMethodInvoker implements Comparable<AbstractMethodInvoker>, MethodInvoker {

	protected final Method method;
	protected final ParameterResolver<?>[] parameterValueResolvers;
	private final Score score;

	public AbstractMethodInvoker(Method method, ParameterResolver<?>[] parameterValueResolvers) {
		this.method = method;
		this.parameterValueResolvers = parameterValueResolvers;
		this.score = new Score(method);
	}

	public Object invoke(Object target, Object param) {
		Assert.isTrue(method.getDeclaringClass().isInstance(target),
				"Given target is not an instance of the method's owner.");
		Assert.notNull(param, "'param' may not be null");
		Object[] parameterValues = new Object[getParameterValueResolvers().length];
		for (int i = 0; i < parameterValues.length; i++) {
			parameterValues[i] = getParameterValueResolvers()[i].resolveParameterValue(param);
		}
		try {
			return doInvoke(target, parameterValues);
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract Object doInvoke(Object target, Object[] args) throws Exception;

	public Method getMethod() {
		return this.method;
	}

	public String getMethodName() {
		return this.method.getName();
	}

	@Override
	public int compareTo(AbstractMethodInvoker o) {
		return score.compareTo(o.score);
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof AbstractEventHandler) && ((AbstractMethodInvoker) obj).score.equals(score);
	}

	@Override
	public int hashCode() {
		return score.hashCode();
	}

	/**
	 * Returns the parameter resolvers provided at construction time.
	 * 
	 * @return the parameter resolvers provided at construction time
	 */
	protected ParameterResolver<?>[] getParameterValueResolvers() {
		return parameterValueResolvers;
	}

	private static final class Score implements Comparable<Score> {

		private final int declarationDepth;
		private final String methodName;

		private Score(Method method) {
			declarationDepth = superClassCount(method.getDeclaringClass(), 0);
			methodName = method.getName();
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
			} else {
				return methodName.compareTo(o.methodName);
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

			if (methodName != score.methodName) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = declarationDepth;
			result = 31 * result + methodName.hashCode();
			return result;
		}
	}

}
