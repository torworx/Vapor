package evymind.vapor.core.reflect;

import java.lang.reflect.Method;
import java.util.Set;

import com.google.common.collect.Sets;

public class ObjectMethodsExcludeFilter implements MethodFilter {
	
	private final Set<String> objectMethods = Sets.newHashSet();
	
	public ObjectMethodsExcludeFilter() {
		Method[] methods = Object.class.getDeclaredMethods();
		for (Method method : methods) {
			objectMethods.add(method.getName());
		}
	}

	@Override
	public boolean isHandled(Method m) {
		return !objectMethods.contains(m.getName());
	}
}
