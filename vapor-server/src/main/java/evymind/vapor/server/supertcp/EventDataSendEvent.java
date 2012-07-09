package evymind.vapor.server.supertcp;

import java.util.UUID;

import evymind.vapor.core.VaporBuffer;

public class EventDataSendEvent {
	
	private final SCServerWorker worker;
	private final UUID destination;
	private final VaporBuffer data;

	public EventDataSendEvent(SCServerWorker worker, UUID destination, VaporBuffer data) {
		this.worker = worker;
		this.destination = destination;
		this.data = data;
	}

	public SCServerWorker getWorker() {
		return worker;
	}

	public UUID getDestination() {
		return destination;
	}

	public VaporBuffer getData() {
		return data;
	}

}
