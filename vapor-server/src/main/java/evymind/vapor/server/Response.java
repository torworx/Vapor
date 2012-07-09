package evymind.vapor.server;

import evymind.vapor.core.Message;
import evymind.vapor.core.Transport;
import evymind.vapor.core.VaporBuffer;

public class Response implements ServiceResponse {
	
	private Transport transport;
	
	private VaporBuffer data;
	private Message message;
	
	protected void recycle() {
		
	}
	
	protected void reset() {
		transport = null;
		data = null;
		message = null;
	}

	public Transport getTransport() {
		return transport;
	}

	public void setTransport(Transport transport) {
		this.transport = transport;
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

}
