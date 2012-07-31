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
import evymind.vapor.zeroconf.utils.DNSUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Copyright 2012 EvyMind.
 */
public abstract class BaseZeroConfTest {

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

    protected String getServiceType() {
        ServiceDefinition[] sds = ServerUtils.getServiceDefinitions(server);
        for (ServiceDefinition sd : sds) {
            Assert.assertTrue(sd.getAliasNames().size() > 0);
            for (String type : sd.getAliasNames()) {
                return type;
            }
        }
        throw new RuntimeException("No service defined in server");
    }

    protected String getDomain() {
        return "local.";
    }

    protected void assertHasDNS(String type) throws Exception {
        assertHasDNS(type, "local");
    }
    protected void assertHasDNS(String type, String domain) throws Exception {
        JmDNS mdns = JmDNS.create();
        ServiceInfo[] infos = mdns.list(DNSUtils.qualify(type, domain));
        Assert.assertTrue(infos != null && infos.length > 0);
        mdns.close();
    }

    protected void assertHasServices(Server server) throws Exception {
        ServiceDefinition[] sds = ServerUtils.getServiceDefinitions(server);
        Assert.assertNotNull(sds);
        Assert.assertTrue(sds.length > 0);
        for (ServiceDefinition sd : sds) {
            Assert.assertTrue(sd.getAliasNames().size() > 0);
            for (String type : sd.getAliasNames()) {
                assertHasDNS(type);
            }
        }
    }
}
