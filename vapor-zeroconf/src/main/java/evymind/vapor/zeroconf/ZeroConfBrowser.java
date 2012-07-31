package evymind.vapor.zeroconf;

import evymind.vapor.zeroconf.utils.DNSUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2012 EvyMind.
 */
public class ZeroConfBrowser extends AbstractZeroConf {

    private final List<ZeroConfBrowserListener> listeners = new ArrayList<ZeroConfBrowserListener>();

    private ZeroConfBrowserStrategy browserStrategy;

    private String serviceType;

    public void start() {
        if (prepareBrowserStrategy()) {
            browserStrategy.start(getDomain(), serviceType);
        }
    }

    public void stop() {
        stopBrowserStrategy();
    }

    public ZeroConfStrategy getCurrentStrategy() {
        if (browserStrategy != null) {
            return browserStrategy.getCurrentStrategyType();
        }
        return getStrategy();
    }

    protected boolean prepareBrowserStrategy() {
        stopBrowserStrategy();
        this.browserStrategy = buildBrowserStrategy();
        return this.browserStrategy != null;
    }

    protected ZeroConfBrowserStrategy buildBrowserStrategy() {
        ZeroConfStrategy strategy = getStrategy();
        if (strategy == ZeroConfStrategy.AUTO) {
            if (DNSUtils.checkDNSFunctionsSilent()) {  // Bonjour is present
                strategy = ZeroConfStrategy.BONJOUR;
            }                                    // TODO: Support hub strategy
            else {
                try {
                    DNSUtils.checkDNSFunctions();
                } catch (Exception e) {
                    fireError(e);
                    return null;
                }
            }
        }

        switch (strategy) {
            case BONJOUR:
                return new BonjourBrowserStrategy(this);
            default:
                return null;
        }

    }

    protected void stopBrowserStrategy() {
        if (this.browserStrategy != null) {
            this.browserStrategy.stop();
            this.browserStrategy = null;
        }
    }

    protected void fireError(Exception exception) {
        // TODO: check and create hub strategy and register service
        for (ZeroConfBrowserListener listener : listeners) {
            listener.onError(this, getCurrentStrategy(), exception);
        }
    }

    protected void fireServiceAdded(ZeroConfService record) {
        for (ZeroConfBrowserListener listener : listeners) {
            listener.onServiceAdded(this, record);
        }
    }

    protected void fireServiceRemoved(ZeroConfService record) {
        for (ZeroConfBrowserListener listener : listeners) {
            listener.onServiceRemoved(this, record);
        }
    }

    public void addBrowserListener(ZeroConfBrowserListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeBrowserListener(ZeroConfBrowserListener listener) {
        listeners.remove(listener);
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}
