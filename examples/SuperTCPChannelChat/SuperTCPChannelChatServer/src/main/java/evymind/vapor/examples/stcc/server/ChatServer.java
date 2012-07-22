package evymind.vapor.examples.stcc.server;

import evymind.vapor.core.message.bin.BinMessageFactory;
import evymind.vapor.core.utils.UuidUtils;
import evymind.vapor.examples.stcc.ChatServerService;
import evymind.vapor.examples.stcc.LoginService;
import evymind.vapor.examples.stcc.ServerShutdownEvent;
import evymind.vapor.examples.stcc.impl.ChatServerServiceImpl;
import evymind.vapor.examples.stcc.impl.LoginListener;
import evymind.vapor.examples.stcc.impl.LoginServiceImpl;
import evymind.vapor.server.Server;
import evymind.vapor.server.ServiceContext;
import evymind.vapor.server.ServiceModule;
import evymind.vapor.server.eventrepository.InMemoryEventRepository;
import evymind.vapor.server.supertcp.netty.NettySuperTCPConnector;
import evymind.vapor.service.ServiceContextHandler;

public class ChatServer {
	
	private Server server;
	
	private boolean terminated;
	
	public ChatServer() {
		this(-1);
	}

	public ChatServer(int port) {
		BinMessageFactory messageFactory = new BinMessageFactory();
		
		server = new Server();
		NettySuperTCPConnector connector = new NettySuperTCPConnector();
		if (port > 0) {
			connector.setPort(port);
		}
		connector.addDispatcher(messageFactory);

		server.addConnector(connector);

		InMemoryEventRepository eventRepository = new InMemoryEventRepository();
		eventRepository.setMessageFactory(messageFactory);
		server.setEventRepository(eventRepository);

		ServiceContextHandler handler = new ServiceContextHandler(true);

		handler.configure(new ServiceModule() {

			@Override
			public void configure(ServiceContext context) {
				// Register servers
				context.addService(LoginService.class, LoginServiceImpl.class);
				context.addService(ChatServerService.class, ChatServerServiceImpl.class);
				
				// Register listeners
				context.addListener(LoginListener.class);
			}
		});

		server.setHandler(handler);

	}
	
	public void start() throws Exception {
		server.start();
	}
	
	public void shutdown() throws Exception {
		if (isStarted()) {
			server.getEventRepository().publish(new ServerShutdownEvent(), UuidUtils.EMPTY_UUID);
			Thread.sleep(2000); // Allow clients to gracefully logout
			server.stop();
		}
	}
	
	public boolean isStarted() {
		return server.isStarted();
	}
	
	public boolean isTerminated() {
		return terminated;
	}
	
	public void terminate() {
		this.terminated = true;
	}

}
