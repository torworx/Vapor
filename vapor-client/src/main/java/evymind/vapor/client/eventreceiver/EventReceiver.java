package evymind.vapor.client.eventreceiver;

import evymind.vapor.core.VaporBuffer;

public interface EventReceiver {

	void dispatch(VaporBuffer data);
	
	void subscribe(Object handler);
	
	void unsubscribe(Object handler);
}
