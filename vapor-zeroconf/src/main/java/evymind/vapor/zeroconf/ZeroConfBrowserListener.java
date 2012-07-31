package evymind.vapor.zeroconf;

/**
 * Copyright 2012 EvyMind.
 */
public interface ZeroConfBrowserListener {

    void onError(ZeroConfBrowser browser, ZeroConfStrategy strategy, Exception exception);

    void onServiceAdded(ZeroConfBrowser browser, ZeroConfService record);

    void onServiceRemoved(ZeroConfBrowser browser, ZeroConfService record);
}
