package evymind.vapor.client.supertcp;

import evymind.vapor.core.VaporBuffer;

public class EventDataReceivedEvent {
	
	private final VaporBuffer eventData;

	public EventDataReceivedEvent(VaporBuffer eventData) {
		this.eventData = eventData;
	}

	public VaporBuffer getEventData() {
		return eventData;
	}
	
}
