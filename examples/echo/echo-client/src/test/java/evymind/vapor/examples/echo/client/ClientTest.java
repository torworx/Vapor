package evymind.vapor.examples.echo.client;

import evymind.vapor.client.proxy.ServiceProxyFactory;
import evymind.vapor.client.proxy.utils.ServiceProxyUtils;
import evymind.vapor.client.supertcp.SuperTCPChannel;
import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.message.bin.BinMessageFactory;
import evymind.vapor.examples.echo.api.EchoService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Copyright 2012 EvyMind.
 */
public class ClientTest {

    protected ServiceProxyFactory serviceProxyFactory = ServiceProxyUtils.getDefaultServiceProxyFactory();

    private SuperTCPChannel channel;
    private EchoService service;
    private MessageFactory<?> messageFactory = new BinMessageFactory();

    @Before
    public void setup() throws InterruptedException {
        channel = new SuperTCPChannel();
        // For debug
        channel.connect("localhost");

        service = serviceProxyFactory.getService(EchoService.class, messageFactory, channel);
    }

    @After
    public void teardown() throws InterruptedException {
        channel.disconnect();
    }

    @Test
    public void testEcho() {
        Assert.assertEquals("Hello World!", service.echo("Hello World!"));
    }
}
