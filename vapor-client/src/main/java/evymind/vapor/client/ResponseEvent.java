package evymind.vapor.client;

import evymind.vapor.core.VaporBuffer;

public class ResponseEvent {
	
	private VaporBuffer data;

	public ResponseEvent(VaporBuffer data) {
		super();
		this.data = data;
	}

	public VaporBuffer getData() {
		return data;
	}

}
