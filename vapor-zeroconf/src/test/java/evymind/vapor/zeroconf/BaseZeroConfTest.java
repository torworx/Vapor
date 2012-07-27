package evymind.vapor.zeroconf;

import evymind.vapor.server.Server;
import evymind.vapor.server.invoker.ServiceDefinition;
import evymind.vapor.server.utils.ServerUtils;
import org.junit.Assert;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Copyright 2012 EvyMind.
 */
public abstract class BaseZeroConfTest {

    protected String includeDomain(String type, String domain) {
        domain = domain.endsWith(".") ? domain : domain + ".";
        if (type.endsWith(domain)) {
            return type;
        }
        return (type.endsWith(".") ? type : type + ".") + domain;
    }

    protected void assertHasDNS(String type) throws Exception {
        assertHasDNS(type, "local");
    }
    protected void assertHasDNS(String type, String domain) throws Exception {
        JmDNS mdns = JmDNS.create();
        ServiceInfo[] infos = mdns.list(includeDomain(type, domain));
        Assert.assertTrue(infos != null && infos.length > 0);
        mdns.close();
    }

    protected void assertHasServices(Server server) throws Exception {
        ServiceDefinition[] sds = ServerUtils.getServiceDefinitions(server);
        Assert.assertNotNull(sds);
        Assert.assertTrue(sds.length > 0);
        for (ServiceDefinition sd : sds) {
            Assert.assertTrue(sd.getAliasNames().size() > 0);
            for (String type : sd.getAliasNames()) {
                assertHasDNS(type);
            }
        }
    }
}
