package evymind.vapor.zeroconf;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.Enumeration;

/**
 * Copyright 2012 EvyMind.
 */
public class BonjourResolver implements ZeroConfResolver {

    private static BonjourResolver instance;

    public static BonjourResolver getInstance() {
        if (instance == null) {
            instance = new BonjourResolver();
        }
        return instance;
    }

    protected boolean doResolve(final ZeroConfService confService, int timeout) {
        try {
            JmDNS mdns = JmDNS.create();
            mdns.addServiceListener(confService.getFullServiceType(), new ServiceListener() {

                @Override
                public void serviceAdded(ServiceEvent event) {
                    // no-op
                }

                @Override
                public void serviceRemoved(ServiceEvent event) {
                    // no-op
                }

                @Override
                public void serviceResolved(ServiceEvent event) {
                    resolveService(confService, event.getInfo());
                }
            });
            mdns.requestServiceInfo(confService.getFullServiceType(), confService.getServiceName(), timeout);
            mdns.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void resolve(ZeroConfService confService, int timeout) {
        doResolve(confService, timeout);
    }

    @Override
    public boolean tryResolve(ZeroConfService confService, int timeout) {
        return doResolve(confService, timeout);
    }

    public static void resolveService(ZeroConfService service, ServiceInfo serviceInfo) {
        String[] hostAddresses = serviceInfo.getHostAddresses();
        if (hostAddresses != null && serviceInfo.getHostAddresses().length > 0) {
            service.setHostTarget(hostAddresses[0]);
        }
        for (Enumeration<String> names = serviceInfo.getPropertyNames(); names.hasMoreElements();) {
            String name = names.nextElement();
            service.setProperty(name, serviceInfo.getPropertyString(name));
        }
        Inet6Address[] inet6Addresses = serviceInfo.getInet6Addresses();
        if (inet6Addresses != null && inet6Addresses.length > 0) {
            service.setIpv6Address(inet6Addresses[0].getHostAddress());
        }
        Inet4Address[] inet4Addresses = serviceInfo.getInet4Addresses();
        if (inet4Addresses != null && inet4Addresses.length > 0) {
            service.setIpv4Address(inet4Addresses[0].getHostAddress());
        }
        service.setPort(serviceInfo.getPort());
        service.markResolved();
    }
}
