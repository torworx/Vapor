package evymind.vapor.core.reflect;

import java.lang.reflect.Method;

public interface MethodInvoker {

	Object invoke(Object target, Object param);
	
	Method getMethod();
	
	String getMethodName();
	
}
