package evymind.vapor.client.eventreceiver;

import java.util.Map;
import java.util.WeakHashMap;

import evymind.vapor.client.AbstractChannelAware;
import evymind.vapor.core.Message;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.event.handling.EventBus;
import evymind.vapor.core.event.handling.SimpleEventBus;
import evymind.vapor.core.event.handling.annontation.AnnotationEventListenerAdapter;

public class DefaultEventReceiver extends AbstractChannelAware implements EventReceiver {
	
	private EventBus eventBus = new SimpleEventBus();
	private Map<Object, AnnotationEventListenerAdapter> adaptors = new WeakHashMap<Object, AnnotationEventListenerAdapter>();

	@Override
	public void dispatch(VaporBuffer data) {
		Message message = getMessageFactory().createMessage();
		message.readFromBuffer(data);
		Object event = message.readObject("event");
		eventBus.publish(event);
	}
	
	public void subscribe(Object handler) {
		if (!adaptors.containsKey(handler)) {
			AnnotationEventListenerAdapter adapter = AnnotationEventListenerAdapter.subscribe(handler, eventBus);
			adaptors.put(handler, adapter);
		}
	}
	
	public void unsubscribe(Object handler) {
		if (adaptors.containsKey(handler)) {
			eventBus.unsubscribe(adaptors.get(handler));
		}
	}

}
