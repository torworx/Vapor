package evymind.vapor.zeroconf;

import evymind.vapor.core.utils.component.AbstractLifecycle;
import evymind.vapor.core.utils.component.Lifecycle;
import evymind.vapor.server.*;
import evymind.vapor.server.invoker.ServiceDefinition;
import evymind.vapor.server.utils.ServerUtils;
import evymind.vapor.zeroconf.utils.DNSUtils;
import evymind.vapor.zeroconf.utils.OS;

import java.util.*;

/**
 * Copyright 2012 EvyMind.
 */
public class ZeroConfRegistration extends AbstractZeroConf {

    private final ZeroConfServerListener serverListener;

    private final List<ZeroConfRegistrationEngine> registrationEngines = new ArrayList<ZeroConfRegistrationEngine>();

    private final List<ZeroConfRegistrationListener> listeners = new ArrayList<ZeroConfRegistrationListener>();

    private ZeroConfRegistrationStrategy registrationStrategy;

    private Server server;

    public ZeroConfRegistration() {
        this.serverListener = new ZeroConfServerListener();
    }

    protected void doAfterOpen(Server server) {
        if (!prepareRegistrationStrategy()) {
            return;
        }
        Map<Connector, Map<String, ?>> channelsProps = buildServerChannelsProps(server);
        registerAllServices(server, channelsProps);
    }

    protected void doBeforeClose(Server server) {
        registrationEngines.clear();
        closeRegistrationStrategy();
    }

    protected boolean prepareRegistrationStrategy() {
        closeRegistrationStrategy();
        this.registrationStrategy = buildRegistrationStrategy();
        return this.registrationStrategy != null;
    }

    protected ZeroConfRegistrationStrategy buildRegistrationStrategy() {
        ZeroConfEngine engine = getEngine();
        if (engine == ZeroConfEngine.AUTO) {
            if (DNSUtils.checkDNSFunctionsSilent()) {  // Bonjour is present
                engine = ZeroConfEngine.BONJOUR;
            }                                    // TODO: Support hub strategy
            else {
                try {
                    DNSUtils.checkDNSFunctions();
                } catch (Exception e) {
                    registrationStrategyFailed(e);
                    return null;
                }
            }
        }

        switch (engine) {
            case BONJOUR:
                return new BonjourRegistrationStrategy();
            default:
                return null;
        }

    }

    protected void registrationStrategyFailed(Exception exception) {
        // TODO: check and create hub strategy and register service
        try {
            fireRegistrationFailed(getEngine(), exception);
        } finally {
        }
    }

    protected void closeRegistrationStrategy() {
        if (this.registrationStrategy != null) {
            this.registrationStrategy.close();
            this.registrationStrategy = null;
        }
    }

    protected Map<Connector, Map<String, ?>> buildServerChannelsProps(Server server) {
        Map<Connector, Map<String, ?>> channelsProps = new LinkedHashMap<Connector, Map<String, ?>>();
        if (server != null) {
            Connector[] connectors = server.getConnectors();
            for (Connector connector : connectors) {
                if (connector.isZeroConf()) {
                    channelsProps.put(connector, buildServerChannelProps(connector));
                }
            }
        }
        return channelsProps;
    }

    protected void registerAllServices(Server server, Map<Connector, Map<String, ?>> channelsProps) {
        ServiceDefinition[] sds = ServerUtils.getServiceDefinitions(server);
        if (sds != null && sds.length > 0) {
            for (Map.Entry<Connector, Map<String, ?>> entry : channelsProps.entrySet()) {
                for (ServiceDefinition sd : sds) {
                    registerServices(server, entry.getKey(), entry.getValue(), sd);
                }
            }
        }
    }

    protected void registerServices(Server server, Connector connector, Map<String, ?> props, ServiceDefinition serviceDefinition) {
        for (String serviceType : serviceDefinition.getAliasNames()) {
             ZeroConfRegistrationEngine registrationEngine = new ZeroConfRegistrationEngine(this);
            registrationEngines.add(registrationEngine);

            registrationEngine.registerService(getDomain(), serviceType, serviceDefinition.getServiceInterface().getSimpleName(),
                    connector.getPort(), props);
        }
    }

    private Map<String, ?> buildServerChannelProps(Connector connector) {
        Map<String, Object> record = new HashMap<String, Object>();
        record.put("txtvers", "1");
        record.put("platform", OS.OS_NAME);
        record.put("channel", connector.getConnectorType().toString());
        String messages = "";
        for (MessageDispatcher dispatcher : connector.getDispatchers()) {
            if (messages != null && !messages.trim().equals("")) {
                messages += ",";
            }
            messages += dispatcher.getMessageFactory().getName();
        }
        record.put("messages", messages);
        return record;
    }

    public ZeroConfRegistrationStrategy getRegistrationStrategy() {
        return registrationStrategy;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        if (this.server != server) {
            if (this.server != null) {
                this.server.removeLifecycleListener(serverListener);
                doBeforeClose(this.server);
                // this.server.setZeroConfRegistrationServer(nil);
            }
            this.server = null;
            if (server != null) {
                this.server = server;
                // this.server.setZeroConfRegistrationServer(this);
                if (this.server.isStarted()) {
                    doAfterOpen(this.server);
                }
                this.server.addLifecycleListener(serverListener);
            }
        }
    }

    public void addRegistrationListener(ZeroConfRegistrationListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeRegistrationListener(ZeroConfRegistrationListener listener) {
        listeners.remove(listener);
    }

    protected void fireRegistrationFailed(ZeroConfEngine engine, Exception exception) {
        for (ZeroConfRegistrationListener listener : listeners) {
            listener.registrationFailed(this, engine, exception);
        }
    }

    protected void fireRegistrationSucceeded(ZeroConfEngine engine, Exception exception) {
        for (ZeroConfRegistrationListener listener : listeners) {
            listener.registrationSucceeded(this, engine);
        }
    }

    class ZeroConfServerListener extends AbstractLifecycle.AbstractLifecycleListener {

        @Override
        public void lifecycleStarted(Lifecycle event) {
            ZeroConfRegistration.this.doAfterOpen(ZeroConfRegistration.this.server);
        }

        @Override
        public void lifecycleStopping(Lifecycle event) {
            ZeroConfRegistration.this.doBeforeClose(ZeroConfRegistration.this.server);
        }
    }
}
