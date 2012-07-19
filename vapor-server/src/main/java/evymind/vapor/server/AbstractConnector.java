package evymind.vapor.server;

import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.VaporRuntimeException;
import evymind.vapor.core.Transport;
import evymind.vapor.core.event.handling.EventBus;
import evymind.vapor.core.utils.component.AggregateLifecycle;
import evymind.vapor.core.utils.log.Logs;
import evymind.vapor.server.eventrepository.EventRepository;

public abstract class AbstractConnector extends AggregateLifecycle implements Connector, ServerHolder {

	private static final Logger log = LoggerFactory.getLogger(AbstractConnector.class);

	private final MessageDispatchers dispatchers;

	private String name;
	private Server server;
	private String host;
	private int port;

	private EventBus eventBus;
	private EventRepository eventRepository;

	public AbstractConnector() {
		super();
		this.dispatchers = createDispatchers(this);
	}

	protected MessageDispatchers createDispatchers(ServerHolder serverHolder) {
		return new MessageDispatchers(serverHolder);
	}


	@Override
	protected void doStart() throws Exception {
		if (server == null) {
			throw new IllegalStateException("No server");
		}

		// open listener port
		open();

		if (eventBus == null) {
			eventBus = server.getEventBus();
			addBean(eventBus, false);
		}

		super.doStart();

		log.info("Started {}", this);
	}


	@Override
	protected void doStop() throws Exception {
		try {
			close();
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}

		super.doStop();
	}

	public void dispatchMessage(Transport transport, Request request, Response response) {
		try {
			MessageDispatcher dispatcher = dispatchers.findDispatcher(transport, request);
			if (dispatcher == null) {
				throw new VaporRuntimeException("Cannot find message dispatcher.");
			}
			doDispatchMessage(dispatcher, transport, request, response);
		} catch (Exception e) {
			log.warn(Logs.IGNORED, e);
			// TODO check writeUTF or writeString
			response.getData().writeUTF(e.getMessage());
		}
	}

	protected void doDispatchMessage(MessageDispatcher dispatcher, Transport transport, Request request,
			Response response) {
		// TODO decrypt request if use encryption
		dispatcher.processMessage(transport, request, response);
		// TODO encrypt response if use encryption
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	@Override
	public String getName() {
		if (name == null) {
			name = (getHost() == null ? "0.0.0.0" : getHost()) + ":" + getPort();
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public EventBus getEventBus() {
		return eventBus;
	}

	public void setEventBus(EventBus eventBus) {
		if (!Objects.equal(this.eventBus, eventBus)) {
			if (this.eventBus != null) {
				removeBean(this.eventBus);
			}
			this.eventBus = eventBus;
			if (this.eventBus != null) {
				addBean(this.eventBus);
			}
		}
	}

	public void addDispatcher(MessageFactory<?> messageFactory) {
		dispatchers.addDispatcher(messageFactory);
	}

	public Collection<MessageDispatcher> getDispatchers() {
		return dispatchers.getDispatchers();
	}

	public void setDispatchers(Collection<MessageDispatcher> dispatchers) {
		this.dispatchers.setDispatchers(dispatchers);
	}

	public EventRepository getEventRepository() {
		return eventRepository != null ? eventRepository : (getServer() != null ? getServer().getEventRepository()
				: null);
	}

	public void setEventRepository(EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}


	@Override
	public String toString() {
		return String.format("%s@%s:%d", getClass().getSimpleName(), getHost() == null ? "0.0.0.0" : getHost(),
				getPort());
	}
}
