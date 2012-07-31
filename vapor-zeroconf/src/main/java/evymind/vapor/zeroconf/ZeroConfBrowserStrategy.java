package evymind.vapor.zeroconf;

/**
 * Copyright 2012 EvyMind.
 */
public interface ZeroConfBrowserStrategy {

    ZeroConfStrategy getCurrentStrategyType();

    void start(String domain, String type);

    void stop();
}
