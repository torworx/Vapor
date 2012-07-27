package evymind.vapor.server.utils;

import evymind.vapor.server.Handler;
import evymind.vapor.server.HandlerContainer;
import evymind.vapor.server.Server;
import evymind.vapor.server.invoker.ServiceDefinition;
import evymind.vapor.service.ServiceHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2012 EvyMind.
 */
public final class ServerUtils {

    private ServerUtils() {
    }

    public static ServiceDefinition[] getServiceDefinitions(Server server) {
        ServiceHandler[] shs = null;
        Handler handler = server.getHandler();
        if (handler instanceof HandlerContainer) {
            shs = ((HandlerContainer) handler).getChildHandlersByClass(ServiceHandler.class);
        }

        if (shs == null) {
            return new ServiceDefinition[0];
        }

        List<ServiceDefinition> sds = new ArrayList<ServiceDefinition>();
        for (ServiceHandler sh : shs) {
            for (ServiceDefinition sd : sh.getServices()) {
                sds.add(sd);
            }
        }
        return sds.toArray(new ServiceDefinition[sds.size()]);
    }
}
