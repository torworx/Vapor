package evymind.vapor.core.utils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import evyframework.common.Assert;
import evymind.vapor.core.VaporRuntimeException;

public class CachedReflections {
	
	private static CachedReflections instance;
	
	private Map<Object, Method> methodCache = new HashMap<Object, Method>();
	
	private CachedReflections() {};
	
	public static CachedReflections instance() {
		if (instance == null) {
			instance = new CachedReflections();
		}
		return instance;
	}
	
	public Method getMethod(Class<?> clazz, String name) {
		Method method = findMethod(clazz, name);
		if (method == null) {
			throw new VaporRuntimeException(String.format("Unknown method %s for class %s", name, clazz.getName()));
		}
		if (!method.isAccessible()) {
			method.setAccessible(true);
		}
		return method;
	}
	
	public Method findMethod(Class<?> clazz, String name) {
		return doFindMethod(clazz, name, null);
	}
	
	public Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
		return doFindMethod(clazz, name, paramTypes);
	}
	
	private Method doFindMethod(Class<?> clazz, String name, Class<?>[] paramTypes) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(name, "Method name must not be null");
		Class<?> searchType = clazz;
		while (searchType != null) {
			int hashcode = HashCodeUtils.build(clazz, name, paramTypes);
			if (methodCache.containsKey(hashcode)) {
				return methodCache.get(hashcode);
			} else {
			
				Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
				for (Method method : methods) {
					if (name.equals(method.getName())
							&& (paramTypes == null || Arrays.equals(paramTypes, method.getParameterTypes()))) {
						methodCache.put(hashcode, method);
						return method;
					}
				}
				searchType = searchType.getSuperclass();
			}
		}
		return null;
	}

}
