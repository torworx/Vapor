package evymind.vapor.zeroconf;

import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyright 2012 EvyMind.
 */
public class ZeroConfBrowserTest extends BaseZeroConfTest {

    @Test
    public void testBrowse() throws Exception {
        ZeroConfRegistration registration = new ZeroConfRegistration();
        registration.setServer(server);
        server.start();

        ZeroConfBrowser browser = new ZeroConfBrowser();
        browser.setDomain(getDomain());
        browser.setServiceType(getServiceType());

        final List<ZeroConfService> services = new ArrayList<ZeroConfService>();
        browser.addBrowserListener(new ZeroConfBrowserListener() {
            @Override
            public void onError(ZeroConfBrowser browser, ZeroConfStrategy strategy, Exception exception) {
                System.out.println(exception);
            }

            @Override
            public void onServiceAdded(ZeroConfBrowser browser, ZeroConfService record) {
                services.add(record);
                System.out.println(System.currentTimeMillis());
                System.out.println("Added Service: " + record);
            }

            @Override
            public void onServiceRemoved(ZeroConfBrowser browser, ZeroConfService record) {
                services.remove(record);
                System.out.println("Removed Service: " + record);
            }
        });
        browser.start();

        Thread.sleep(6000);
        Assert.assertFalse(services.isEmpty());
        ZeroConfService service = services.get(0);
        Assert.assertTrue(service.isResolved());
        browser.stop();
    }
}
