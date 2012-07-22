package evymind.vapor.server;

import java.util.UUID;

import evymind.vapor.core.Message;
import evymind.vapor.core.Transport;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.server.handler.ContextHandler.Context;

public class Request implements ServiceRequest {
	
	private Connector connector;
	private Transport transport;
	
	private Context context;
	private boolean newContext;
	
	private int requestId;
	private UUID clientId;
	
	private String requestInterface;
	private String requestMethod;
	
	private Session session;
	private SessionManager sessionManager;
	
	private String remoteAddress;
	private int remotePort;
	
	private VaporBuffer data;
	private Message message;
	
	private boolean handled;
	
	protected void recycle() {
		reset();
	}
	
	public void reset() {
		connector = null;
		transport = null;
		context = null;
		newContext = false;
		requestId = 0;
		clientId = null;
		session = null;
		sessionManager = null;
		remoteAddress = null;
		remotePort = 0;
		data = null;
		message = null;
		handled = false;
	}

	@Override
	public Connector getConnector() {
		return connector;
	}
	
	public void setConnector(Connector connector) {
		this.connector= connector;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Transport> T getTransport() {
		return (T) transport;
	}

	public void setTransport(Transport transport) {
		this.transport = transport;
	}

	public Context getContext() {
		return context;
	}

    /**
     * @return True if this is the first call of {@link #takeNewContext()} since the last
     *         {@link #setContext(ContextHandler.Context)} call.
     */
	public void setContext(Context context) {
		this.newContext = this.context != context;
		this.context = context;
	}
	
	public boolean takeNewContext() {
		boolean answer = this.newContext;
		this.newContext = false;
		return answer;
	}

	@Override
	public int getRequestId() {
		return requestId;
	}
	
	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	@Override
	public UUID getClientId() {
		return clientId;
	}
	
	public void setClientId(UUID clientId) {
		this.clientId = clientId;
	}

	public String getRequestInterface() {
		return requestInterface;
	}

	public void setRequestInterface(String requestInterface) {
		this.requestInterface = requestInterface;
	}

	public String getRequestMethod() {
		return requestMethod;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	@Override
	public Session getSession(boolean create) {
		if ((session == null) && (sessionManager != null)) {
			session = sessionManager.getSession(clientId.toString());
		}
		
		if (session != null) {
			if (sessionManager != null && !sessionManager.isValid(session))
				session = null;
			else
				return session;
		}

		if (!create)
			return null;

		if (sessionManager == null) {
			throw new IllegalStateException("No SessionManager");
		}
		
		session = sessionManager.createSession(getClientId().toString());
		return session;
	}

	@Override
	public Session getSession() {
		return getSession(true);
	}
	
	public void setSession(Session session) {
		this.session = session;
	}

	public SessionManager getSessionManager() {
		return this.sessionManager;
	}
	
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Override
	public String getRemoteAddress() {
		return remoteAddress;
	}
	
	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	@Override
	public int getRemotePort() {
		return remotePort;
	}
	
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public VaporBuffer getData() {
		return data;
	}

	public void setData(VaporBuffer data) {
		this.data = data;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public boolean isHandled() {
		return handled;
	}
	
	public void markHandled() {
		setHandled(true);
	}

	public void setHandled(boolean handled) {
		this.handled = handled;
	}

}
