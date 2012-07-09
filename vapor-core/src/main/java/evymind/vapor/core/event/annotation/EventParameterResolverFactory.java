package evymind.vapor.core.event.annotation;

import java.lang.annotation.Annotation;

import evymind.vapor.core.event.Event;
import evymind.vapor.core.reflect.ParameterResolver;
import evymind.vapor.core.reflect.ParameterResolverFactory;

/**
 * Factory for the default parameter resolvers. This factory is capable for providing parameter resolvers for Event,
 * MetaData and @MetaData annotated parameters.
 *
 * @author Allard Buijze
 * @since 2.0
 */
@SuppressWarnings("rawtypes")
class EventParameterResolverFactory extends ParameterResolverFactory {
    
	@Override
    public ParameterResolver createInstance(Annotation[] methodAnnotations, Class<?> parameterType,
                                            Annotation[] parameterAnnotations) {
        if (Event.class.isAssignableFrom(parameterType)) {
            return new EventParameterResolver(parameterType);
        }
//        if (getAnnotation(parameterAnnotations, MetaData.class) != null) {
//            return new AnnotatedMetaDataParameterResolver(CollectionUtils.getAnnotation(parameterAnnotations,
//                                                                                        MetaData.class), parameterType);
//        }
//        if (org.axonframework.domain.MetaData.class.isAssignableFrom(parameterType)) {
//            return MetaDataParameterResolver.INSTANCE;
//        }
        return null;
    }

    /**
     * Creates a new payload resolver, which passes a event's payload as parameter.
     *
     * @param parameterType The type of payload supported by this resolver
     * @return a payload resolver that returns the payload of a event when of the given <code>parameterType</code>
     */
    public ParameterResolver createPayloadResolver(Class<?> parameterType) {
        return new PayloadParameterResolver(parameterType);
    }

//    private static class AnnotatedMetaDataParameterResolver implements ParameterResolver {
//
//        private final MetaData metaData;
//        private final Class parameterType;
//
//        public AnnotatedMetaDataParameterResolver(MetaData metaData, Class parameterType) {
//            this.metaData = metaData;
//            this.parameterType = parameterType;
//        }
//
//        @Override
//        public Object resolveParameterValue(Event event) {
//            return event.getMetaData().get(metaData.value());
//        }
//
//        @Override
//        public boolean matches(Event event) {
//            return !(parameterType.isPrimitive() || metaData.required())
//                    || (
//                    event.getMetaData().containsKey(metaData.value())
//                            && parameterType.isInstance(event.getMetaData().get(metaData.value()))
//            );
//        }
//    }
//
//    private static final class MetaDataParameterResolver implements ParameterResolver {
//
//        private static final MetaDataParameterResolver INSTANCE = new MetaDataParameterResolver();
//
//        private MetaDataParameterResolver() {
//        }
//
//        @Override
//        public Object resolveParameterValue(Event event) {
//            return event.getMetaData();
//        }
//
//        @Override
//        public boolean matches(Event event) {
//            return true;
//        }
//    }

    private static class EventParameterResolver implements ParameterResolver {

        private final Class<?> parameterType;

        public EventParameterResolver(Class<?> parameterType) {
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

    private static class PayloadParameterResolver implements ParameterResolver {

        private final Class<?> payloadType;

        public PayloadParameterResolver(Class<?> payloadType) {
            this.payloadType = payloadType;
        }

        @Override
        public Object resolveParameterValue(Object object) {
            return ((Event) object).getPayload();
        }

        @Override
        public boolean matches(Object object) {
            return ((Event) object).getPayloadType() != null && payloadType.isAssignableFrom(((Event) object).getPayloadType());
        }
    }
}
