package evymind.vapor.zeroconf;

import evymind.vapor.zeroconf.utils.DNSUtils;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright 2012 EvyMind.
 */
public class ZeroConfService {

    private ZeroConfBrowser browser;
    private ZeroConfResolver resolver;
    private String domain;
    private String serviceName;
    private String serviceType;
    private IpVersion ipVersion;

    private String hostTarget;
    private String ipv4Address;
    private String ipv6Address;
    private final Map<String, String> properties = new HashMap<String, String>();
    private long port;

    private boolean resolved;

    public ZeroConfService(ZeroConfBrowser browser, ZeroConfResolver resolver, String domain, String serviceName, String serviceType) {
        this(browser, resolver, domain, serviceName, serviceType, IpVersion.ANY);
    }

    public ZeroConfService(ZeroConfBrowser browser, ZeroConfResolver resolver, String domain, String serviceName, String serviceType, IpVersion ipVersion) {
        this.browser = browser;
        this.resolver = resolver;
        this.domain = domain;
        this.serviceName = serviceName;
        this.serviceType = serviceType;
        this.ipVersion = ipVersion;
    }

    public void resolve() {
        resolve(6000);
    }

    public void resolve(int timeout) {
        resolver.resolve(this, timeout);
    }

    public boolean tryResolve() {
        return tryResolve(6000);
    }

    public boolean tryResolve(int timeout) {
        return resolver.tryResolve(this, timeout);
    }

    protected void checkResolved() {
        if (!resolved) throw new IllegalStateException("Not resolved yet");
    }

    public ZeroConfBrowser getBrowser() {
        return browser;
    }

    public String getDomain() {
        return domain;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getFullServiceType() {
        return DNSUtils.qualify(serviceType, domain);
    }

    public IpVersion getIpVersion() {
        return ipVersion;
    }

    public String getAddress() {
        checkResolved();
        if (IpVersion.IPv6.equals(ipVersion) && (ipv6Address != null)) {
            return ipv6Address;
        } else {
            return ipv4Address;
        }
    }

    public String getHostTarget() {
        checkResolved();
        return hostTarget;
    }

    public String getIpv4Address() {
        checkResolved();
        return ipv4Address;
    }

    public String getIpv6Address() {
        checkResolved();
        return ipv6Address;
    }

    public long getPort() {
        checkResolved();
        return port;
    }

    public Map<String, String> getProperties() {
        checkResolved();
        return Collections.unmodifiableMap(properties);
    }

    protected void setProperty(String name, String value) {
        properties.put(name, value);
    }

    protected void setHostTarget(String hostTarget) {
        this.hostTarget = hostTarget;
    }

    protected void setIpv4Address(String ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    protected void setIpv6Address(String ipv6Address) {
        this.ipv6Address = ipv6Address;
    }

    protected void setPort(long port) {
        this.port = port;
    }

    public boolean isResolved() {
        return resolved;
    }

    protected void markResolved() {
        resolved = true;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("[" + this.getClass().getSimpleName() + "@" + System.identityHashCode(this));
        buf.append(" name: '");
        buf.append(this.getServiceName()).append("'");
        buf.append(" host: '");
        buf.append(this.getHostTarget()).append("'");
        buf.append(" address: '");
        String address = getAddress();
        if (address != null) {
            buf.append(address);
            buf.append(':');

        } else {
            buf.append("(null):");
        }
        buf.append(this.getPort()).append("'");

        buf.append("' has ");
        buf.append(!this.properties.isEmpty() ? "" : "NO ");
        buf.append("data");
        if (!properties.isEmpty()) {
            buf.append("\n");
            for (String key : properties.keySet()) {
                buf.append("\t" + key + ": '" + properties.get(key) + "'\n");
            }
        } else {
            buf.append(" empty");
        }
        buf.append(']');
        return buf.toString();
    }
}