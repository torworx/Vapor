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

package evymind.vapor.core.event.handling.annontation;

import java.lang.reflect.InvocationTargetException;

import evymind.vapor.core.event.Event;
import evymind.vapor.core.event.annotation.EventHandlerInvoker;

/**
 * Utility class that supports invocation of specific handler methods for a given event. See {@link EventHandler} for
 * the rules for resolving the appropriate method.
 *
 * @author Allard Buijze
 * @see EventHandler
 * @since 0.1
 */
@SuppressWarnings("rawtypes")
public class AnnotationEventHandlerInvoker {

    private final EventHandlerInvoker invoker;

    /**
     * Initialize an event handler invoker that invokes handlers on the given <code>target</code>.
     *
     * @param target the bean on which to invoke event handlers
     */
    public AnnotationEventHandlerInvoker(Object target) {
        invoker = new EventHandlerInvoker(target, EventHandler.class);
    }

    /**
     * Invoke the appropriate @EventHandler for the given <code>event</code>
     *
     * @param event The event transporting the event
     */
	public void invokeEventHandlerMethod(Event event) {
        try {
            invoker.invokeHandlerMethod(event);
        } catch (IllegalAccessException e) {
            throw new EventHandlerInvocationException("Access to the event handler method was denied.", e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new EventHandlerInvocationException("An exception occurred while invoking the handler method.", e);
        }
    }

    /**
     * Returns the target instance containing the @EventHandler annotated methods.
     *
     * @return the target instance containing the @EventHandler annotated methods
     */
    public Object getTarget() {
        return invoker.getTarget();
    }

}
