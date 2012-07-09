package evymind.vapor.client.supertcp;

import evymind.vapor.core.event.handling.annontation.EventHandler;

public class EventDataReceivedHandler {
	
	private final BaseSuperTCPChannel channel;

	public EventDataReceivedHandler(BaseSuperTCPChannel channel) {
		this.channel = channel;
	}
	
	@EventHandler
	public void handleEventDataReceivedEvent(EventDataReceivedEvent event) {
		channel.fireEvent(event.getEventData());
	}
}
