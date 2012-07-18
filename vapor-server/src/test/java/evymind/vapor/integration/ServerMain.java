package evymind.vapor.integration;

import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.message.bin.BinMessage;
import evymind.vapor.core.message.bin.BinMessageFactory;
import evymind.vapor.server.Server;
import evymind.vapor.server.ServiceContext;
import evymind.vapor.server.ServiceModule;
import evymind.vapor.server.eventrepository.InMemoryEventRepository;
import evymind.vapor.server.supertcp.netty.NettySuperTCPConnector;
import evymind.vapor.service.ServiceContextHandler;
import evymind.vapor.service.api.MegaDemoService;
import evymind.vapor.service.impl.MegaDemoServiceImpl;

public class ServerMain {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        MessageFactory<BinMessage> messageFactory = new BinMessageFactory();

        Server server = new Server();
        NettySuperTCPConnector connector = new NettySuperTCPConnector();
        connector.addDispatcher(messageFactory);

        server.addConnector(connector);

        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        eventRepository.setMessageFactory(messageFactory);
        server.setEventRepository(eventRepository);

        ServiceContextHandler handler = new ServiceContextHandler();

        handler.configure(new ServiceModule() {

            @Override
            public void configure(ServiceContext context) {
                MegaDemoServiceImpl service = new MegaDemoServiceImpl();
                context.addService(MegaDemoService.class, service);
                context.addListener(service);
            }
        });


        server.setHandler(handler);

        server.start();
    }

}
