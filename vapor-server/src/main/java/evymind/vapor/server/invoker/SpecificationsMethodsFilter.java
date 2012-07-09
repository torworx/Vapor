package evymind.vapor.server.invoker;

import java.lang.reflect.Method;
import java.util.Set;

import com.google.common.collect.Sets;

import evyframework.common.ReflectionUtils;
import evymind.vapor.core.reflect.ObjectMethodsExcludeFilter;

public class SpecificationsMethodsFilter extends ObjectMethodsExcludeFilter {
	
	private final Set<String> specificaitonMethods;

	protected SpecificationsMethodsFilter(Class<?>... specifications) {
		this.specificaitonMethods = getMethods(specifications);
	}
	
	protected Set<String> getMethods(Class<?>... classes) {
		Set<String> answer = Sets.newHashSet();
		if (classes != null && classes.length > 0) {
			for (Class<?> cls : classes) {
				Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(cls);
				for (Method	m : methods) {
					answer.add(m.getName());
				}
			}
		}
		return answer;
	}

	@Override
	public boolean isHandled(Method m) {
		if (!super.isHandled(m)) {
			return false;
		}
		if (specificaitonMethods.isEmpty()) {
			return true;
		}
		return specificaitonMethods.contains(m.getName());
	}
	
	

}
