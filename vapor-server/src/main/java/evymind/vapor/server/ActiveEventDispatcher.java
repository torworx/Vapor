package evymind.vapor.server;

import java.util.UUID;

import evymind.vapor.core.VaporBuffer;

public interface ActiveEventDispatcher {
	
	void eventsSubscribed(UUID client);
	
	void dispatchEvent(UUID destination, VaporBuffer eventData);
}
