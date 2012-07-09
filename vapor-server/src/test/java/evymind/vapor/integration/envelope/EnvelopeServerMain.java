package evymind.vapor.integration.envelope;

import evymind.vapor.server.Server;
import evymind.vapor.server.eventrepository.InMemoryEventRepository;
import evymind.vapor.server.supertcp.netty.NettySuperTCPConnector;
import evymind.vapor.service.ServiceContextHandler;
import evymind.vapor.service.api.MegaDemoService;
import evymind.vapor.service.impl.MegaDemoServiceImpl;

public class EnvelopeServerMain extends EnvelopeTestBase {

	public void run() throws Exception {
		initMessageFactory();
		
		Server server = new Server();
		NettySuperTCPConnector connector = new NettySuperTCPConnector();
		connector.addDispatcher(messageFactory);
		
		server.addConnector(connector);

		InMemoryEventRepository eventRepository = new InMemoryEventRepository();
		eventRepository.setMessageFactory(messageFactory);
		server.setEventRepository(eventRepository);

		MegaDemoServiceImpl service = new MegaDemoServiceImpl();
		ServiceContextHandler handler = new ServiceContextHandler();
		handler.addService(MegaDemoService.class, service);
		handler.addListener(service);
		
		server.setHandler(handler);

		server.start();
	}

	public static void main(String[] args) throws Exception {
		new EnvelopeServerMain().run();
	}
}
