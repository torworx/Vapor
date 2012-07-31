package javax.jmdns;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2012 EvyMind.
 */
public class JmDNSTest {

    private String type = "_test._tcp.local.";

    @Test
    public void testTutorial() throws Exception {
        final JmDNS mdnsRegistration = JmDNS.create();
        ServiceInfo serviceInfo = ServiceInfo.create(type, "ServiceTest", 0,
                "test from mac");
        mdnsRegistration.registerService(serviceInfo);

        final JmDNS mdnsBrowser = JmDNS.create();
        final List<ServiceInfo> infos = new ArrayList<ServiceInfo>();
        ServiceListener listener = new ServiceListener() {
            public void serviceResolved(ServiceEvent ev) {
                System.out.println("Service resolved: " + ev.getInfo());
                infos.add(ev.getInfo());
            }

            public void serviceRemoved(ServiceEvent ev) {
                System.out.println("Service removed: " + ev.getInfo());
                infos.remove(ev.getInfo());
            }

            public void serviceAdded(ServiceEvent event) {
                // Required to force serviceResolved to be called again
                // (after the first search)
                mdnsBrowser.requestServiceInfo(event.getType(), event.getName(), 1);
            }
        };
        mdnsBrowser.addServiceListener(type, listener);

        Thread.sleep(6000);

        Assert.assertFalse(infos.isEmpty());
        Assert.assertEquals(infos.get(0), serviceInfo);

        mdnsRegistration.close();

        Thread.sleep(6000);

        Assert.assertTrue(infos.isEmpty());

        mdnsRegistration.close();
        mdnsBrowser.close();
    }
}
