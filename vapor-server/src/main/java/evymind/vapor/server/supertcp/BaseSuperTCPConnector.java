package evymind.vapor.server.supertcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;

import evymind.vapor.core.event.handling.EventBus;
import evymind.vapor.core.event.handling.annontation.AnnotationEventListenerAdapter;
import evymind.vapor.core.event.handling.disruptor.DisruptorEventBus;
import evymind.vapor.core.supertcp.PackageAck;
import evymind.vapor.server.AbstractConnector;
import evymind.vapor.server.Request;
import evymind.vapor.server.RequestPool;
import evymind.vapor.server.Response;
import evymind.vapor.server.ResponsePool;

public abstract class BaseSuperTCPConnector extends AbstractConnector {
	
	private static final Logger log = LoggerFactory.getLogger(BaseSuperTCPConnector.class);
	
	private EventBus eventDispatchBus;
	
	private RequestPool requestPool = new RequestPool();
	private ResponsePool responsePool = new ResponsePool();
	
	private ClientManager clientManager;
	
	private RequestHandler requestHandler = new RequestHandler(this);
	private EventDataSendHandler eventDataSendHandler = new EventDataSendHandler(this);
	
	private int ackWaitTimeout = 10000;
	private int maxPackageSize = 10 * 1024 * 1024;
	private boolean skipAck = false;
	private boolean blockingEvents = false;
	private boolean keepAlive = true;
	private boolean tcpNoDelay = true;
	
	public BaseSuperTCPConnector() {
		super();
		clientManager = createClientManager(); 
		setPort(8095);
	}

	public RequestPool getRequestPool() {
		return requestPool;
	}

	public ResponsePool getResponsePool() {
		return responsePool;
	}

	protected abstract ClientManager createClientManager();
	
	public ClientManager getClientManager() {
		if (this.clientManager == null) {
			this.clientManager = createClientManager();
		}
		return this.clientManager;
	}
	
	@Override
	protected void doStart() throws Exception {
		getClientManager().start();
		if (eventDispatchBus == null) {
			setEventDispatchBus(new DisruptorEventBus());
		}
		super.doStart();
		AnnotationEventListenerAdapter.subscribe(requestHandler, getEventBus());
		AnnotationEventListenerAdapter.subscribe(eventDataSendHandler, getEventDispatchBus());
	}
	
	@Override
	protected void doStop() throws Exception {
		super.doStop();
		getClientManager().stop();
	}

	protected void handleRequest(Request request, Response response) {
		log.debug("Publishing request[id={}] to event bus", request.getRequestId());
		SCServerWorker worker = request.getTransport();
		try {
			getEventBus().publish(new RequestEvent(request, response));
		} catch (RuntimeException e) {
			log.debug("Request[id={}] publish error, send QUEUE_FULL ack to client", request.getRequestId());
			worker.sendError(request.getRequestId(), PackageAck.NOACK_QUEUE_FULL);
			throw e;
		}
	}
	
	protected void handleEventDataSend(EventDataSendEvent event) {
		if (!clientManager.isValid(event.getWorker())) {
			throw new TransportInvalidException("Transport invalid : " + event.getWorker());
		}
		if (isBlockingEvents()) {
			log.debug("Block sending event to client [{}]", event.getDestination());
			eventDataSendHandler.handleEventDataSendEvent(event);
			log.debug("Block sended event to client [{}]", event.getDestination());
		} else {
			log.debug("Submiting event data to client [{}] use event bus [{}]", event.getDestination(), getEventBus());
			getEventDispatchBus().publish(event);
			log.debug("Submited event data to client [{}] use event bus [{}]", event.getDestination(), getEventBus());
		}
	}
	
	protected void connected(SCServerWorker worker) {
		getServer().getHandler().connected(worker, worker.getClientId());
		clientManager.add(worker);
		// TODO dispatch events stored in event repository
	}
	
	protected void disconnected(SCServerWorker worker) {
		clientManager.remove(worker);
		getServer().getHandler().disconnected(worker, worker.getClientId());
	}
	
	public EventBus getEventDispatchBus() {
		return eventDispatchBus != null ? eventDispatchBus : getEventBus();
	}

	public void setEventDispatchBus(EventBus eventDispatchBus) {
		if (!Objects.equal(this.eventDispatchBus, eventDispatchBus)) {
			if (this.eventDispatchBus != null) {
				removeBean(this.eventDispatchBus);
			}
			this.eventDispatchBus = eventDispatchBus;
			if (this.eventDispatchBus != null) {
				addBean(this.eventDispatchBus);
			}
		}
	}

	public String getDefaultResponse() {
		return "ERSC: Invalid connection string";
	}
	
	public int getAckWaitTimeout() {
		return ackWaitTimeout;
	}

	public void setAckWaitTimeout(int ackWaitTimeout) {
		this.ackWaitTimeout = ackWaitTimeout;
	}

	public int getMaxPackageSize() {
		return maxPackageSize;
	}

	public void setMaxPackageSize(int maxPackageSize) {
		this.maxPackageSize = maxPackageSize;
	}

	public boolean isSkipAck() {
		return skipAck;
	}

	public void setSkipAck(boolean skipAck) {
		this.skipAck = skipAck;
	}

	public boolean isBlockingEvents() {
		return blockingEvents;
	}

	public void setBlockingEvents(boolean blockingEvents) {
		this.blockingEvents = blockingEvents;
	}

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public boolean isTcpNoDelay() {
		return tcpNoDelay;
	}

	public void setTcpNoDelay(boolean tcpNoDelay) {
		this.tcpNoDelay = tcpNoDelay;
	}
}
