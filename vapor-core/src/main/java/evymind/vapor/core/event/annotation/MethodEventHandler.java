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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.Assert;

import evyframework.common.ReflectionUtils;
import evymind.vapor.core.event.Event;
import evymind.vapor.core.reflect.ParameterResolver;

import static java.lang.String.format;

/**
 * Represents a method recognized as a handler by the handler inspector (see
 * {@link AbstractEventHandlerInspector}).
 * 
 * @author Allard Buijze
 * @since 2.0
 */
@SuppressWarnings("rawtypes")
public final class MethodEventHandler extends AbstractEventHandler {
	private static final Logger log = LoggerFactory.getLogger(MethodEventHandler.class);
	private final Method method;

	/**
	 * Creates a MethodEventHandler for the given <code>method</code>.
	 * 
	 * @param method
	 *            The method to create a Handler for
	 * @return The MethodEventHandler implementation for the given method.
	 * 
	 * @throws UnsupportedHandlerException
	 *             if the given method is not suitable as a Handler
	 */
	public static MethodEventHandler createFor(Method method) {
		ParameterResolver[] resolvers = findResolvers(method.getAnnotations(), method.getParameterTypes(),
				method.getParameterAnnotations());
		Class<?> firstParameter = method.getParameterTypes()[0];
		Class payloadType;
		if (Event.class.isAssignableFrom(firstParameter)) {
			payloadType = Object.class;
		} else {
			payloadType = firstParameter;
		}
		ReflectionUtils.makeAccessible(method);
		validate(method, resolvers);
		return new MethodEventHandler(method, resolvers, payloadType);
	}

	private static void validate(Method method, ParameterResolver[] parameterResolvers) {
		if (method.getParameterTypes()[0].isPrimitive()) {
			throw new UnsupportedHandlerException(format("The first parameter of %s may not be a primitive type",
					method.toGenericString()), method);
		}
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			if (parameterResolvers[i] == null) {
				throw new UnsupportedHandlerException(format(
						"On method %s, parameter %s is invalid. It is not of any format supported by a provided"
								+ "ParameterValueResolver.", method.toGenericString(), i + 1), method);
			}
		}

		/*
		 * special case: methods with equal signature on EventListener must be
		 * rejected, because it interferes with the Proxy mechanism
		 */
		if (method.getName().equals("handle") && Arrays.equals(method.getParameterTypes(), new Class[] { Event.class })) {
			throw new UnsupportedHandlerException(String.format(
					"Event Handling class %s contains method %s that has a naming conflict with a "
							+ "method on the EventHandler interface. Please rename the method.", method
							.getDeclaringClass().getSimpleName(), method.getName()), method);
		}
	}

	private MethodEventHandler(Method method, ParameterResolver[] parameterValueResolvers, Class payloadType) {
		super(payloadType, method.getDeclaringClass(), parameterValueResolvers);
		this.method = method;
	}

	@Override
    public Object invoke(Object target, Event event) throws InvocationTargetException, IllegalAccessException {
        Assert.isTrue(method.getDeclaringClass().isInstance(target),
                      "Given target is not an instance of the method's owner.");
        Assert.notNull(event, "Event may not be null");
        Object[] parameterValues = new Object[getParameterValueResolvers().length];
        for (int i = 0; i < parameterValues.length; i++) {
            parameterValues[i] = getParameterValueResolvers()[i].resolveParameterValue(event);
        }
        log.debug("Invoking method " + method + " with values " + parameterValues);
        return method.invoke(target, parameterValues);
    }

	/**
	 * Returns the name of the method backing this handler.
	 * 
	 * @return the name of the method backing this handler
	 */
	public String getMethodName() {
		return method.getName();
	}

	/**
	 * Returns the Method backing this handler.
	 * 
	 * @return the Method backing this handler
	 */
	public Method getMethod() {
		return method;
	}

	@Override
	public String toString() {
		return format("HandlerMethod %s.%s for payload type %s: %s", method.getDeclaringClass().getSimpleName(),
				method.getName(), getPayloadType().getSimpleName(), method.toGenericString());
	}
}
