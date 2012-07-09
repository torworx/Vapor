/*
 * Copyright (c) 2010-2011. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package evymind.vapor.core.event.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

import evymind.vapor.core.event.Event;

/**
 * Abstract class to support implementations that need to invoke methods based on an annotation.
 *
 * @author Allard Buijze
 * @since 0.6
 */
@SuppressWarnings("rawtypes") 
public final class EventHandlerInvoker extends AbstractEventHandlerInspector {

    private final Object target;

    /**
     * Initialize a handler invoker for the given <code>target</code> object that has handler method annotated with
     * given <code>annotationType</code>.
     *
     * @param target         The target to invoke methods on
     * @param annotationType The type of annotation used to demarcate the handler methods
     */
    public EventHandlerInvoker(Object target, Class<? extends Annotation> annotationType) {
        super(target.getClass(), annotationType);
        this.target = target;
    }

    /**
     * Invoke the handler demarcated with the given <code>annotationClass</code> on the target for the given
     * <code>event</code>.
     *
     * @param parameter the event to handle
     * @return the return value of the invocation
     *
     * @throws IllegalAccessException    when the security manager does not allow the invocation
     * @throws InvocationTargetException when the handler throws a checked Exception
     */
    public Object invokeHandlerMethod(Event parameter) throws InvocationTargetException, IllegalAccessException {
        return invokeHandlerMethod(parameter, NullReturningCallback.INSTANCE);
    }

    /**
     * Invoke the handler demarcated with the given <code>annotationClass</code> on the target for the given
     * <code>event</code>. If no suitable handler is found, the given <code>onNoMethodFound</code> callback is invoked.
     *
     * @param parameter       the event to handle
     * @param onNoMethodFound what to do when no such handler is found.
     * @return the return value of the invocation
     *
     * @throws IllegalAccessException    when the security manager does not allow the invocation
     * @throws InvocationTargetException when the handler throws a checked Exception
     */
    public Object invokeHandlerMethod(Event parameter, NoMethodFoundCallback onNoMethodFound)
            throws InvocationTargetException, IllegalAccessException {
        MethodEventHandler m = findHandlerMethod(parameter);
        if (m == null) {
            // event listener doesn't support this type of event
            return onNoMethodFound.onNoMethodFound(parameter);
        }

        return m.invoke(target, parameter);
    }

    /**
     * Returns the target on which handler methods are invoked.
     *
     * @return the target on which handler methods are invoked
     */
    public Object getTarget() {
        return target;
    }

    /**
     * Callback used in cases where the handler did not find a suitable method to invoke.
     */
    public interface NoMethodFoundCallback<T extends Event> {

        /**
         * Indicates what needs to happen when no handler is found for a given parameter. The default behavior is to
         * return <code>null</code>.
         *
         * @param parameter The parameter for which no handler could be found
         * @return the value to return when no handler method is found. Defaults to <code>null</code>.
         */
        Object onNoMethodFound(Event parameter);
    }

    private static class NullReturningCallback implements NoMethodFoundCallback {

        private static final NullReturningCallback INSTANCE = new NullReturningCallback();

        @Override
        public Object onNoMethodFound(Event parameter) {
            return null;
        }
    }
}
