package evymind.vapor.server.invoker;

import java.lang.annotation.Annotation;

import evymind.vapor.core.Message;
import evymind.vapor.core.reflect.ParameterResolver;
import evymind.vapor.core.reflect.ParameterResolverFactory;
import evymind.vapor.server.ServiceRequest;

public class ServiceParameterResolverFactory extends ParameterResolverFactory {

	@SuppressWarnings("rawtypes")
	@Override
	protected ParameterResolver createInstance(Annotation[] memberAnnotations, Class<?> parameterType,
			Annotation[] parameterAnnotations) {
		// TODO parameter name support
		if (ServiceRequest.class.isAssignableFrom(parameterType)) {
			return new ServiceRequestParameterResolver(parameterType);
		}
		return new RequestMessageParameterResolver("", parameterType);
	}
	
	private static class ServiceRequestParameterResolver implements ParameterResolver<ServiceRequest> {
		
		private final Class<?> parameterType;

		public ServiceRequestParameterResolver(Class<?> parameterType) {
			this.parameterType = parameterType;
		}

		@Override
		public ServiceRequest resolveParameterValue(Object object) {
			return (ServiceRequest) object;
		}

		@Override
		public boolean matches(Object object) {
			return parameterType.isInstance(object);
		}
		
	}

	private static class RequestMessageParameterResolver implements ParameterResolver<Object> {

		private final String parameterName;
		private final Class<?> parameterType;

		public RequestMessageParameterResolver(String parameterName, Class<?> parameterType) {
			this.parameterName = parameterName;
			this.parameterType = parameterType;
		}

		@Override
		public Object resolveParameterValue(Object object) {
			Message message = null;
			if (object instanceof ServiceRequest) {
				message = ((ServiceRequest) object).getMessage();
			} else if (object instanceof Message) {
				message = (Message) object;
			}
			return message.read(parameterName, parameterType);
		}

		@Override
		public boolean matches(Object object) {
			return object instanceof Message || object instanceof ServiceRequest;
		}
	}

}
