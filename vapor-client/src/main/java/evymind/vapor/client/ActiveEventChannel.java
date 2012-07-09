package evymind.vapor.client;

import evymind.vapor.client.eventreceiver.EventReceiver;

public interface ActiveEventChannel {
	
	void registerEventReceiver(EventReceiver receiver);
	
	void unregisterEventReceiver(EventReceiver receiver);

}
