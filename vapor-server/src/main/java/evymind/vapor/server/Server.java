package evymind.vapor.server;

import java.io.IOException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.CollectionUtils;
import evyframework.common.collect.LazyList;
import evyframework.common.exception.MultiException;
import evymind.vapor.core.event.handling.EventBus;
import evymind.vapor.core.event.handling.RunnableEventListener;
import evymind.vapor.core.event.handling.disruptor.DisruptorEventBus;
import evymind.vapor.core.utils.Attributes;
import evymind.vapor.core.utils.AttributesMap;
import evymind.vapor.core.utils.component.Container;
import evymind.vapor.core.utils.thread.ShutdownThread;
import evymind.vapor.server.eventrepository.EventRepository;
import evymind.vapor.server.eventrepository.InMemoryEventRepository;

public class Server extends ServerInternalHandler implements Attributes {

    private static final Logger log = LoggerFactory.getLogger(Server.class);

    private static final String VERSION;

    static {
        if (Server.class.getPackage() != null && Server.class.getPackage().getImplementationVersion() != null)
            VERSION = Server.class.getPackage().getImplementationVersion();
        else
            VERSION = System.getProperty("vapor.version", "1.y.z-SNAPSHOT");
    }

    private final Container container = new Container();
    private final AttributesMap attributes = new AttributesMap();
    private EventBus eventBus;
    private Connector[] connectors;
    private EventRepository eventRepository;
    private int graceful = 0;
    private boolean stopAtShutdown;
    private boolean dumpAfterStart = false;
    private boolean dumpBeforeStop = false;

    public Server() {
        setServer(this);
    }

    public static String getVersion() {
        return VERSION;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        if (this.eventBus != null) {
            removeBean(this.eventBus);
        }
        container.update(this, this.eventBus, eventBus, "eventbus", false);
        this.eventBus = eventBus;
        if (this.eventBus != null) {
            // register RunnableEventListener for support Runnable event publish
            this.eventBus.subscribe(RunnableEventListener.INSTANCE);
            addBean(this.eventBus);
        }
    }

    public Container getContainer() {
        return container;
    }

    public boolean isStopAtShutdown() {
        return stopAtShutdown;
    }

    public void setStopAtShutdown(boolean stopAtShutdown) {
        this.stopAtShutdown = stopAtShutdown;
        if (stopAtShutdown) {
            ShutdownThread.register(this);
        } else {
            ShutdownThread.unregister(this);
        }
    }


    /**
     * @return Returns the connectors.
     */
    public Connector[] getConnectors() {
        return connectors;
    }


    public void addConnector(Connector connector) {
        setConnectors((Connector[]) LazyList.addToArray(getConnectors(), connector, Connector.class));
    }


    /**
     * Conveniance method which calls {@link #getConnectors()} and {@link #setConnectors(Connector[])} to remove a
     * connector.
     *
     * @param connector The connector to remove.
     */
    public void removeConnector(Connector connector) {
        setConnectors((Connector[]) LazyList.removeFromArray(getConnectors(), connector));
    }


    /**
     * Set the connectors for this server. Each connector has this server set as it's ThreadPool and its Handler.
     *
     * @param connectors The connectors to set.
     */
    public void setConnectors(Connector[] connectors) {
        if (connectors != null) {
            for (Connector connector : connectors) {
                connector.setServer(this);
            }
        }

        container.update(this, this.connectors, connectors, "connector");
        this.connectors = connectors;
    }

    protected final void doStart() throws Exception {
        if (isStopAtShutdown()) {
            ShutdownThread.register(this);
        }
        log.info("vapor-" + VERSION);

        MultiException mex = new MultiException();

        if (eventBus == null) {
            setEventBus(new DisruptorEventBus());
        }

        try {
            super.doStart();
        } catch (Exception e) {
            mex.add(e);
        }

        if (connectors != null && mex.size() == 0) {
            for (Connector connector : connectors) {
                try {
                    connector.start();
                } catch (Exception e) {
                    mex.add(e);
                }
            }
        }

        if (isDumpAfterStart()) {
            dumpStdErr();
        }

        mex.ifExceptionThrow();
    }

    protected final void doStop() throws Exception {
        if (isDumpBeforeStop()) {
            dumpStdErr();
        }

        MultiException mex = new MultiException();

        if (graceful > 0) {
            if (connectors != null) {
                for (Connector connector : connectors) {
                    log.info("Graceful shutdown {}", connector);
                    try {
                        connector.stop();
                    } catch (Exception e) {
                        mex.add(e);
                    }
                }
            }
            Handler[] handlers = getChildHandlersByClass(Graceful.class);
            for (int c = 0; c < handlers.length; c++) {
                Graceful handler = (Graceful) handlers[c];
                log.info("Graceful shutdown {}", handler);
                handler.setShutdown(true);
            }
            Thread.sleep(graceful);
        }

        if (connectors != null) {
            for (Connector connector : connectors) {
                try {
                    connector.stop();
                } catch (Exception e) {
                    mex.add(e);
                }
            }
        }

        try {
            super.doStop();
        } catch (Throwable e) {
            mex.add(e);
        }

        mex.ifExceptionThrow();

        if (isStopAtShutdown()) {
            ShutdownThread.unregister(this);
        }
    }


    /**
     * Add an associated bean. The bean will be added to the servers {@link Container} and if it is a {@link evymind.vapor.core.utils.component.Lifecycle}
     * instance, it will be started/stopped along with the Server. Any beans that are also {@link evymind.vapor.core.utils.component.Destroyable}, will be
     * destroyed with the server.
     *
     * @param o the bean object to add
     */
    @Override
    public boolean addBean(Object o) {
        if (super.addBean(o)) {
            container.addBean(o);
            return true;
        }
        return false;
    }


    /**
     * Remove an associated bean.
     */
    @Override
    public boolean removeBean(Object o) {
        if (super.removeBean(o)) {
            container.removeBean(o);
            return true;
        }
        return false;
    }


    /*
      * @see org.eclipse.util.AttributesMap#clearAttributes()
      */
    public void clearAttributes() {
        attributes.clearAttributes();
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) attributes.getAttribute(name);
    }

    public Enumeration<String> getAttributeNames() {
        return AttributesMap.getAttributeNamesCopy(attributes);
    }

    public void removeAttribute(String name) {
        attributes.removeAttribute(name);
    }

    public void setAttribute(String name, Object attribute) {
        attributes.setAttribute(name, attribute);
    }


    /**
     * @return the graceful
     */
    public int getGracefulShutdown() {
        return graceful;
    }


    /**
     * Set graceful shutdown timeout. If set, the internal <code>doStop()</code> method will not immediately stop the
     * server. Instead, all Connections will be closed so that new connections will not be accepted and all
     * handlers that implement {@link Graceful} will be put into the shutdown mode so that no new requests will be
     * accepted, but existing requests can complete. The server will then wait the configured timeout before stopping.
     *
     * @param timeoutMS the milliseconds to wait for existing request to complete before stopping the server.
     */
    public void setGracefulShutdown(int timeoutMS) {
        graceful = timeoutMS;
    }

    public boolean isDumpAfterStart() {
        return dumpAfterStart;
    }

    public void setDumpAfterStart(boolean dumpAfterStart) {
        this.dumpAfterStart = dumpAfterStart;
    }

    public boolean isDumpBeforeStop() {
        return dumpBeforeStop;
    }

    public void setDumpBeforeStop(boolean dumpBeforeStop) {
        this.dumpBeforeStop = dumpBeforeStop;
    }


    @Override
    public void dump(Appendable out, String indent) throws IOException {
        dumpThis(out);
        dump(out, indent, CollectionUtils.arrayToList(getHandlers()), getBeans());
    }


    @Override
    public String toString() {
        return this.getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    public EventRepository getEventRepository() {
        if (eventRepository == null) {
            eventRepository = new InMemoryEventRepository();
        }
        return eventRepository;
    }

    public void setEventRepository(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

}
