package javax.jmdns;

import org.junit.Assert;
import org.junit.Test;

/**
 * Copyright 2012 EvyMind.
 */
public class JmDNSTest {

    private String type = "_test._tcp.local.";

    @Test
    public void testTutorial() throws Exception {
        final JmDNS mdns = JmDNS.create();
        ServiceInfo serviceInfo = ServiceInfo.create(type, "ServiceTest", 0,
                "test from mac");
        mdns.registerService(serviceInfo);

        ServiceListener listener = new ServiceListener() {
            public void serviceResolved(ServiceEvent ev) {
                System.out.println("Service resolved: " + ev.getInfo().getQualifiedName()
                        + " host:" + ev.getInfo().getHostAddresses()[0]
                        + " port:" + ev.getInfo().getPort());
            }

            public void serviceRemoved(ServiceEvent ev) {
                System.out.println("Service removed: " + ev.getName());
            }

            public void serviceAdded(ServiceEvent event) {
                // Required to force serviceResolved to be called again
                // (after the first search)
                mdns.requestServiceInfo(event.getType(), event.getName(), 1);
            }
        };
        mdns.addServiceListener(type, listener);
        ServiceInfo[] infos = mdns.list(type);

        Assert.assertTrue(infos.length > 0);
        Assert.assertEquals(infos[0], serviceInfo);

        mdns.removeServiceListener(type, listener);
        mdns.unregisterAllServices();
        mdns.close();
    }
}
