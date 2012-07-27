package evymind.vapor.zeroconf;

import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.message.bin.BinMessage;
import evymind.vapor.core.message.bin.BinMessageFactory;
import evymind.vapor.server.Server;
import evymind.vapor.server.ServiceContext;
import evymind.vapor.server.ServiceModule;
import evymind.vapor.server.eventrepository.InMemoryEventRepository;
import evymind.vapor.server.invoker.ServiceDefinition;
import evymind.vapor.server.supertcp.netty.NettySuperTCPConnector;
import evymind.vapor.server.utils.ServerUtils;
import evymind.vapor.service.ServiceContextHandler;
import evymind.vapor.zeroconf.sample.SampleService;
import evymind.vapor.zeroconf.sample.SampleServiceImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Copyright 2012 EvyMind.
 */
public class ZeroConfRegistrationTest extends BaseZeroConfTest {

    Server server;

    @Before
    public void setUp() throws Exception {
        // Message Type
        MessageFactory<BinMessage> messageFactory = new BinMessageFactory();

        // Server
        server = new Server();

        // Connector
        NettySuperTCPConnector connector = new NettySuperTCPConnector();
        // Enable ZeroConf
        connector.setZeroConf(true);
        connector.addDispatcher(messageFactory);
        server.addConnector(connector);

        // EventRepository
        InMemoryEventRepository eventRepository = new InMemoryEventRepository();
        eventRepository.setMessageFactory(messageFactory);
        server.setEventRepository(eventRepository);

        // Handler
        ServiceContextHandler handler = new ServiceContextHandler();

        handler.configure(new ServiceModule() {

            @Override
            public void configure(ServiceContext context) {
                context.addService(SampleService.class, SampleServiceImpl.class);
            }
        });
        server.setHandler(handler);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testSetServer() throws Exception {
        ZeroConfRegistration registration = new ZeroConfRegistration();
        registration.setServer(server);
        Assert.assertEquals(server, registration.getServer());
    }

    @Test
    public void testSetServerBeforeStart() throws Exception {
        ZeroConfRegistration registration = new ZeroConfRegistration();
        registration.setServer(server);
        Assert.assertEquals(server, registration.getServer());
        server.start();
        assertHasServices(server);
    }

    @Test
    public void testSetServerAfterStart() throws Exception {
        server.start();
        ZeroConfRegistration registration = new ZeroConfRegistration();
        registration.setServer(server);
        Assert.assertEquals(server, registration.getServer());
        assertHasServices(server);
    }
}
