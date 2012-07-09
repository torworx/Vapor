package evymind.vapor.core.reflect;

import java.lang.reflect.Method;

public interface MethodFilter {
	
	boolean isHandled(Method m);
}
