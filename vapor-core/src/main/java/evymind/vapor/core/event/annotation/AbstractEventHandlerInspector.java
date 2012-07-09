package evymind.vapor.core.event.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import evyframework.common.ReflectionUtils;
import evymind.vapor.core.event.Event;

@SuppressWarnings("rawtypes")
public class AbstractEventHandlerInspector {

    private final Class<?> targetType;
    private final SortedSet<MethodEventHandler> handlers = new TreeSet<MethodEventHandler>();

    /**
     * Initialize an AbstractHandlerInspector, where the given <code>annotationType</code> is used to annotate the
     * Handler methods.
     *
     * @param targetType     The targetType to inspect methods on
     * @param annotationType The annotation used on the Event Handler methods.
     */
    protected AbstractEventHandlerInspector(Class<?> targetType, Class<? extends Annotation> annotationType) {
        this.targetType = targetType;
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(targetType);
        for (Method method : methods) {
            if (method.getAnnotation(annotationType) != null) {
                MethodEventHandler eventHandlerMethod = MethodEventHandler.createFor(method);
                if (!handlers.add(eventHandlerMethod)) {
                    MethodEventHandler existing = handlers.tailSet(eventHandlerMethod).first();
                    throw new UnsupportedHandlerException(
                            String.format("The class %s contains two handler methods (%s and %s) that listen "
                                                  + "to the same Event type: %s",
                                          method.getDeclaringClass().getSimpleName(),
                                          eventHandlerMethod.getMethodName(),
                                          existing.getMethodName(),
                                          eventHandlerMethod.getPayloadType().getSimpleName()), method);
                }
            }
        }
    }

    /**
     * Returns the handler method that handles objects of the given <code>parameterType</code>. Returns
     * <code>null</code> is no such method is found.
     *
     * @param event The event to find a handler for
     * @return the  handler method for the given parameterType
     */
	public MethodEventHandler findHandlerMethod(final Event event) {
        for (MethodEventHandler handler : handlers) {
            if (handler.matches(event)) {
                return handler;
            }
        }
        return null;
    }

    /**
     * Returns the list of handlers found on target type.
     *
     * @return the list of handlers found on target type
     */
    public List<MethodEventHandler> getHandlers() {
        return new ArrayList<MethodEventHandler>(handlers);
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
