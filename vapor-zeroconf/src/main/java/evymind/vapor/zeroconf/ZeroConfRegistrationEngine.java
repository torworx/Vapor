package evymind.vapor.zeroconf;

import evymind.vapor.zeroconf.utils.DNSUtils;

import java.util.Map;

/**
 * Copyright 2012 EvyMind.
 */
public class ZeroConfRegistrationEngine {

    private ZeroConfRegistration registration;

    private String domain;
    private String serviceType;
    private String serviceName;
    private int port;
    private Map<String, ?> txtRecord;

    public ZeroConfRegistrationEngine(ZeroConfRegistration registration) {
        this.registration = registration;
    }

    public void registerService(String domain, String serviceType, String serviceName, int port, Map<String, ?> txtRecord) {
        this.domain = domain;
        this.serviceType = serviceType;
        this.serviceName = serviceName;
        this.port = port;
        this.txtRecord = txtRecord;

        doRegisterService();
    }

    protected void doRegisterService() {
        if (registration.getRegistrationStrategy() == null) {
            return;
        }
        registration.getRegistrationStrategy().registerService(domain, serviceType, serviceName, port, txtRecord);
    }

}
