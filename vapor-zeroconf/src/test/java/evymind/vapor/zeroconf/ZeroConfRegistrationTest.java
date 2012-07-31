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
