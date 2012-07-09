package evymind.vapor.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import evymind.vapor.client.eventreceiver.EventReceiver;
import evymind.vapor.core.VaporBuffer;

public abstract class AbstractActiveEventChannel extends AbstractTransportChannel implements ActiveEventChannel {

	private List<EventReceiver> eventReceivers = new ArrayList<EventReceiver>();
	
	protected void fireEvent(VaporBuffer data) {
		if (!eventReceivers.isEmpty()) {
			for (EventReceiver receiver : eventReceivers) {
				receiver.dispatch(data);
			}
		}
	}

	@Override
	public void registerEventReceiver(EventReceiver receiver) {
		if (!eventReceivers.contains(receiver)) {
			eventReceivers.add(receiver);
			if (receiver instanceof ChannelAware) {
				((ChannelAware) receiver).setChannel(this);
			}
		}
	}

	@Override
	public void unregisterEventReceiver(EventReceiver receiver) {
		eventReceivers.remove(receiver);
	}
	
	public void unregisterEventReceivers() {
		eventReceivers.clear();
	}
	
	public Collection<EventReceiver> getEventReceivers() {
		return eventReceivers;
	}
	
}
