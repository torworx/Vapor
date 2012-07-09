package evymind.vapor.reflect;

import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;

import evymind.vapor.core.reflect.ClassInvoker;
import evymind.vapor.core.reflect.DefaultParameterResolverFactory;
import evymind.vapor.core.reflect.asm.ASMClassInvoker;
import evymind.vapor.core.reflect.jdk.JdkClassInvoker;

public class ASMClassInvokerTest {
	
	private final StubService stubService = new StubServiceImpl();
	
	@Test
	public void testJavaReflectInvoke() throws InvocationTargetException, IllegalAccessException {
		String message = "Hello World!";
		ClassInvoker classInvoker = new JdkClassInvoker(StubServiceImpl.class, new DefaultParameterResolverFactory());
		Object answer = classInvoker.invokeMethod(stubService, "echo", message);
		Assert.assertEquals(message, answer);
	}
	
	@Test
	public void testASMReflectInvoke() throws InvocationTargetException, IllegalAccessException {
		String message = "Hello World!";
		ClassInvoker classInvoker = new ASMClassInvoker(StubServiceImpl.class, new DefaultParameterResolverFactory());
		Object answer = classInvoker.invokeMethod(stubService, "echo", message);
		Assert.assertEquals(message, answer);
	}
	
	
	public interface StubService {
		
		String echo(String message);
	}
	
	public class StubServiceImpl implements StubService {

		@Override
		public String echo(String message) {
			return message;
		}
		
	}

}
