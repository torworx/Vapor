package evymind.vapor.app;

import static org.junit.Assert.*;
import org.junit.Test;

import evyframework.common.io.ClassPathResource;
import evymind.vapor.app.echo.EchoListener;
import evymind.vapor.app.echo.EchoService;
import evymind.vapor.app.echo.EchoServiceImpl;
import evymind.vapor.server.invoker.DefaultServiceInvokerFactory;
import evymind.vapor.server.invoker.ServiceDefinition;
import evymind.vapor.server.invoker.ServiceScope;

public class AppConfigDescriptorProcessorTest {
	
	@Test
	public void testProcess() throws Exception {
		AppConfigDescriptorProcessor processor = new AppConfigDescriptorProcessor();
		AppContext appContext = new AppContext();
		processor.process(appContext, new AppDescriptorContext(new ClassPathResource("evymind/vapor/app/app.yml")));
		
		DefaultServiceInvokerFactory serviceInvokerFactory = (DefaultServiceInvokerFactory) appContext.getServiceHandler().getServiceInvokerFactory();
		assertTrue(serviceInvokerFactory.getServiceDefinitions().size() > 0);
		
		ServiceDefinition definition = serviceInvokerFactory.getServiceDefinitions().get(0);
		assertEquals(EchoService.class, definition.getServiceInterface());
		assertEquals(EchoServiceImpl.class, definition.getServiceImplementation());
		assertEquals(ServiceScope.singleton, definition.getScope());
		assertNull(definition.getServiceInstance());
		
		assertTrue(appContext.getListeners().length > 0);
		assertEquals(EchoListener.class, appContext.getListeners()[0].getClass());
	}

}
