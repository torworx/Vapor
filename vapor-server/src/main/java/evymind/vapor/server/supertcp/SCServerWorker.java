package evymind.vapor.server.supertcp;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.core.TCPTransport;
import evymind.vapor.core.Transport;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.VaporBuffers;
import evymind.vapor.core.supertcp.SuperChannelWorker;
import evymind.vapor.core.supertcp.SuperConnection;
import evymind.vapor.server.ActiveEventDispatcher;
import evymind.vapor.server.Request;
import evymind.vapor.server.RequestHolder;
import evymind.vapor.server.Response;

public class SCServerWorker extends SuperChannelWorker implements Transport, TCPTransport, ActiveEventDispatcher {

	private static final Logger log = LoggerFactory.getLogger(SCServerWorker.class);
	
	private BaseSuperTCPConnector connector;

	private int lastId;

	public SCServerWorker(BaseSuperTCPConnector connector, SuperConnection connection) {
		super(connection);
		this.connector = connector;
		this.skipAck = connector.isSkipAck();
	}

	@Override
	public String getDefaultResponse() {
		return connector.getDefaultResponse();
	}

	@Override
	public void processConnect(VaporBuffer buffer) {
		processHandshake(buffer);
		sendHandshake();
		connected();
	}


	@Override
	public void connected() {
		super.connected();
		connector.connected(this);
	}

	@Override
	public void disconnected() {
		super.disconnected();
		connector.disconnected(this);
//		getWorkerManager().removeWorker(this);
	}
	
	@Override
	public void eventsSubscribed(UUID client) {
		// TODO eventsSubscribed
		
	}

	@Override
	public void dispatchEvent(UUID destination, VaporBuffer eventData) {
		connector.handleEventDataSend(new EventDataSendEvent(this, destination, eventData));
	}

	@Override
	protected int doGenerateId() {
		if (--lastId > -1) {
			lastId = -1;
		}
		return lastId;
	}

	@Override
	protected void incomingData(int id, VaporBuffer data) {
		log.debug("Initialize request for package id={}", id);
		Request request = connector.getRequestPool().borrowRequest();
		initRequest(request, id, data);

		Response response = connector.getResponsePool().borrowResponse();
		initResponse(response);

		connector.handleRequest(request, response);
	}

	protected void initRequest(Request request, int id, VaporBuffer data) {
		request.setConnector(connector);
		request.setTransport(this);

		request.setRequestId(id);
		request.setData(data);

		request.setClientId(getClientId());
		request.setRemoteAddress(getRemoteAddress());
		request.setRemotePort(getRemotePort());
	}

	protected void initResponse(Response response) {
		response.setTransport(this);
		// TODO We need a pooled buffer?
		response.setData(VaporBuffers.dynamicBuffer());
	}

	protected void startRequest(Request request) {
		// bind the request to the current thread
		RequestHolder.setServiceRequest(request);
	}

	protected void completeRequest(Request request) {
		// unbind the request from the current thread
		RequestHolder.setServiceRequest(null);
		connector.getRequestPool().returnRequest(request);
	}

	protected void completeResponse(Response response) {
		connector.getResponsePool().returnResponse(response);
	}

	public ClientManager getWorkerManager() {
		return connector.getClientManager();
	}

	@Override
	public String getRemoteAddress() {
		return getConnection().getRemoteAddress();
	}

	@Override
	public int getRemotePort() {
		return getConnection().getRemotePort();
	}

}
