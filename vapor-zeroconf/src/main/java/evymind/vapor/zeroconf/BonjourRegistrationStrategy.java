package evymind.vapor.zeroconf;

import evymind.vapor.zeroconf.utils.DNSUtils;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.util.Map;

/**
 * Copyright 2012 EvyMind.
 */
public class BonjourRegistrationStrategy extends AbstractZeroConfRegistrationStrategy {

    private final JmDNS mdns;
    private boolean closed = false;

    public BonjourRegistrationStrategy() {
        try {
            mdns = JmDNS.create();
        } catch (IOException e) {
            throw new ZeroConfException(e);
        }
    }

    @Override
    public ZeroConfStrategy getCurrentEngineType() {
        return ZeroConfStrategy.BONJOUR;
    }

    @Override
    public void registerService(String domain, String serviceType, String serviceName, int port, Map<String, ?> txtRecord) {
        checkClosed();
        ServiceInfo info = ServiceInfo.create(DNSUtils.qualify(serviceType, domain), serviceName, "", port, 0, 0, false, txtRecord);
        try {
            mdns.registerService(info);
        } catch (IOException e) {
            throw new ZeroConfException(e);
        }
    }

    @Override
    public void close() {
        closed = true;
        try {
            mdns.unregisterAllServices();
            mdns.close();
        } catch (IOException e) {
            throw new ZeroConfException(e);
        }
    }

    protected void checkClosed() {
        if (closed) {
            throw new IllegalStateException(this + " has been closed");
        }
    }
}
