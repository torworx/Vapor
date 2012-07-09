package evymind.vapor.client.supertcp;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.client.AbstractActiveEventChannel;
import evymind.vapor.client.event.ChannelConnectedEvent;
import evymind.vapor.client.event.ChannelDisconnectedEvent;
import evymind.vapor.core.Message;
import evymind.vapor.core.RemotingException;
import evymind.vapor.core.ServerLocator;
import evymind.vapor.core.TCPTransport;
import evymind.vapor.core.TCPTransportProperties;
import evymind.vapor.core.TimeoutException;
import evymind.vapor.core.Transport;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.event.handling.EventBus;
import evymind.vapor.core.event.handling.annontation.AnnotationEventListenerAdapter;
import evymind.vapor.core.event.handling.disruptor.DisruptorEventBus;
import evymind.vapor.core.supertcp.PackageAck;

public abstract class BaseSuperTCPChannel extends AbstractActiveEventChannel implements Transport, TCPTransport,
		TCPTransportProperties {
	
	private static final Logger log = LoggerFactory.getLogger(BaseSuperTCPChannel.class);
	
	protected SCClientWorker worker;
	protected SuperClient connection;
	
	private String host;
	private int port = 8095;
	
	private int connectionTimeout = 10 * 1000;
	private int ackWaitTimeout = 10 * 1000;
	private int requestTimeout = 60 * 1000;
	
	private EventBus eventBus; 
	
	protected Map<Integer, WaitingRequest> waitingRequests = new HashMap<Integer, WaitingRequest>();

	public BaseSuperTCPChannel() {
		super();
		this.worker = new SCClientWorker(this);
		this.connection = createSuperConnection(worker);
	}
	
	protected abstract SuperClient createSuperConnection(SCClientWorker worker);

	@Override
	protected void doSetServerLocator(ServerLocator serverLocator) {
		setHost(serverLocator.getHost());
		setPort(serverLocator.getPort());
	}

	@Override
	public void beforeDispatch(Message message) {
		super.beforeDispatch(message);
		if (!isConnected()) {
			connect();
		}
		if (message != null) {
			message.setClientId(getCliendID());
		}
	}

	@Override
	protected void doDispatch(VaporBuffer request, VaporBuffer response) {
		int id = worker.generateId();
		WaitingRequest req = new WaitingRequest(id);
		waitingRequests.put(id, req);
		try {
			log.debug("==== Before worker.sendPackage");
			SCClientWorker.waitForAck(worker.sendPackage(id, request), getAckWaitTimeout());
			log.debug("==== After worker.sendPackage");
			req.getSignal().await(getRequestTimeout());
			VaporBuffer data = req.getResultData();
			if (data == null) {
				switch (req.getResultErrorCode()) {
				case PackageAck.NOACK_MESSAGE_TOO_LARGE:
					throw new RemotingException("Message from server too large");
				case PackageAck.NOACK_QUEUE_FULL:
					throw new RemotingException("Server queue full");
				default:
					throw new RemotingException("Unknown ack for request id=" + id);
				}
			}
			response.writeBytes(data, data.readableBytes());
			log.debug("==== Got response");
		} catch (InterruptedException e) {
			throw new TimeoutException("Timeout waiting for response", e);
		} finally {
			waitingRequests.remove(req.getId());
		}
		
	}
	
	protected void hasData(int id, VaporBuffer data) {
		if (id < 0) {
			if (eventBus == null) {
				throw new RemotingException("No event bus assigned");
			}
			eventBus.publish(new EventDataReceivedEvent(data));
		} else {
			if (waitingRequests.containsKey(id)) {
				log.debug("Got reponse for request({})", id);
				WaitingRequest req = waitingRequests.get(id);
				req.setResultData(data);
				req.getSignal().signal();
			}
		}
	}
	
	public void connect() {
		connect(host, port);
	}
	
	public void connect(String host) {
		connect(host, port);
	}
	
	public void connect(String host, int port) {
		this.host = host;
		this.port = port;
		try {
			beforeConnect();
			long time = System.currentTimeMillis();
			this.connection.connect(host, port, getConnectionTimeout());
			while (getConnectionTimeout() + time > System.currentTimeMillis()) {
				if (isConnected()) return;
				Thread.sleep(10);
			}
			throw new InterruptedException();
		} catch (InterruptedException e) {
			throw new RemotingException("No connection available");
		}
	}
	
	protected void connected(SCClientWorker worker) {
		multicastEvent(new ChannelConnectedEvent(this, getCliendID()));
	}
	
	protected void disconnected(SCClientWorker worker) {
		multicastEvent(new ChannelDisconnectedEvent(this, getCliendID()));
	}
	
	protected void beforeConnect() {
		initEventBus();
	}
	
	protected void initEventBus() {
		if (eventBus == null) {
			this.eventBus = new DisruptorEventBus();
			AnnotationEventListenerAdapter.subscribe(new EventDataReceivedHandler(this), eventBus);
		}
	}
	
	@Override
	protected void fireEvent(VaporBuffer data) {
		super.fireEvent(data);
	}
	
	public void disconnect() {
		this.worker.disconnect();
	}
	
	public boolean isConnected() {
		return this.worker.isConnected();
	}

	public Map<Integer, WaitingRequest> getWaitingRequests() {
		return waitingRequests;
	}

	public UUID getCliendID() {
		return worker.getClientId();
	}
	
	public void setClientId(UUID clientId) {
		worker.setClientId(clientId);
	}
	
	public int getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public int getAckWaitTimeout() {
		return ackWaitTimeout;
	}

	public void setAckWaitTimeout(int ackWaitTimeout) {
		this.ackWaitTimeout = ackWaitTimeout;
	}

	public int getRequestTimeout() {
		return requestTimeout;
	}

	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}

	public int getMaxPackageSize() {
		return worker.getMaxPackageSize();
	}
	
	public void setMaxPackageSize(int maxPackageSize) {
		worker.setMaxPackageSize(maxPackageSize);
	}
	
	public boolean isSkipAck() {
		return worker.isSkipAck();
	}
	
	public void setSkipAck(boolean skipAck) {
		worker.setSkipAck(skipAck);
	}
	
	@Override
	public String getHost() {
		return host;
	}

	@Override
	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String getRemoteAddress() {
		return this.connection.getRemoteAddress();
	}

	@Override
	public int getRemotePort() {
		return this.connection.getRemotePort();
	}

}
