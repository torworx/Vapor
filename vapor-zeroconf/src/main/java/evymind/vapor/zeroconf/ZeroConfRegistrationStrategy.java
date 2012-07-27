package evymind.vapor.zeroconf;

import java.util.Map;

/**
 * Copyright 2012 EvyMind.
 */
public interface ZeroConfRegistrationStrategy {

    ZeroConfEngine getCurrentEngineType();

    void registerService(String domain, String serviceType, String serviceName, int port, Map<String, ?> txtRecord);

    void close();
}
