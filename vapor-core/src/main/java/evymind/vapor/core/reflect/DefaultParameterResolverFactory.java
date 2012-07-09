package evymind.vapor.core.reflect;

import java.lang.annotation.Annotation;

@SuppressWarnings("rawtypes")
public class DefaultParameterResolverFactory extends ParameterResolverFactory {
	
	@Override
	protected ParameterResolver createInstance(Annotation[] memberAnnotations, Class<?> parameterType,
			Annotation[] parameterAnnotations) {
		return new AssinableParameterResolver(parameterType);
	}
	
	private static class AssinableParameterResolver implements ParameterResolver {

        private final Class<?> parameterType;

        public AssinableParameterResolver(Class<?> parameterType) {
            this.parameterType = parameterType;
        }

        @Override
        public Object resolveParameterValue(Object object) {
            return object;
        }

        @Override
        public boolean matches(Object object) {
            return parameterType.isInstance(object);
        }
    }

}
