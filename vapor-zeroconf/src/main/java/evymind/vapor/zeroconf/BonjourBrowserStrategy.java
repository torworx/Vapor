package evymind.vapor.zeroconf;

import evymind.vapor.zeroconf.utils.DNSUtils;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;

/**
 * Copyright 2012 EvyMind.
 */
public class BonjourBrowserStrategy extends AbstractZeroConfBrowserStrategy {

    private final ZeroConfBrowser browser;
    private JmDNS mdns;

    public BonjourBrowserStrategy(ZeroConfBrowser browser) {
        this.browser = browser;
    }

    @Override
    public ZeroConfStrategy getCurrentStrategyType() {
        return ZeroConfStrategy.BONJOUR;
    }

    @Override
    public void start(String domain, String type) {
        stop();
        try {
            mdns = JmDNS.create();
            String qualifiedType = DNSUtils.qualify(type, domain);
            ServiceListener listener = new BonjourServiceListener(domain, type);
            mdns.addServiceListener(qualifiedType, listener);
        } catch (IOException e) {
            throw new ZeroConfException(e);
        }

    }

    @Override
    public void stop() {
        if (mdns != null) {
            try {
                mdns.close();
            } catch (IOException e) {
                throw new ZeroConfException(e);
            }
        }
    }

    class BonjourServiceListener implements ServiceListener {

        private final String domain;
        private final String type;

        BonjourServiceListener(String domain, String type) {
            this.domain = domain;
            this.type = type;
        }

        @Override
        public void serviceAdded(ServiceEvent event) {
            BonjourBrowserStrategy.this.mdns.requestServiceInfo(event.getType(), event.getName(), 1);
        }

        @Override
        public void serviceRemoved(ServiceEvent event) {
            synchronized (this) {
                BonjourBrowserStrategy.this.browser.fireServiceRemoved(new ZeroConfService(BonjourBrowserStrategy.this.browser,
                        BonjourResolver.getInstance(), event.getInfo().getDomain(), event.getName(), event.getType()));
            }
        }

        @Override
        public void serviceResolved(ServiceEvent event) {
            synchronized (this) {
                ServiceInfo info = event.getInfo();
                ZeroConfService service = new ZeroConfService(BonjourBrowserStrategy.this.browser,
                        BonjourResolver.getInstance(), info.getDomain(), info.getName(), info.getType());
                BonjourResolver.resolveService(service, info);
                BonjourBrowserStrategy.this.browser.fireServiceAdded(service);
            }
        }
    }
}
