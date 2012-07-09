package evymind.vapor.core.event.handling;

import java.util.Map;

import com.google.common.collect.Maps;

import evymind.vapor.core.event.handling.annontation.AnnotationEventListenerAdapter;

public class GenericEventBus {

	private EventBus delegate;
	protected final Map<Object, EventListener> listeners = Maps.newLinkedHashMap();

	public GenericEventBus() {
		this(new SimpleEventBus());
	}

	public GenericEventBus(EventBus delegate) {
		this.delegate = delegate;
	}

	public EventBus getDelegate() {
		return delegate;
	}
	
	public void publish(Object... events) {
		synchronized (listeners) {
			delegate.publish(events);
		}
	}
	
	public Object[] getOriginListeners() {
		return listeners.keySet().toArray(new Object[listeners.size()]);
	}
	
	public void subscribe(Object listener) {
		synchronized (listeners) {
			if (!listeners.containsKey(listener)) {
				if (listener instanceof EventListener) {
					delegate.subscribe((EventListener) listener);
					listeners.put(listener, (EventListener) listener);
				} else {
					EventListener listenerAdapter = AnnotationEventListenerAdapter.subscribe(listener, delegate);
					listeners.put(listener, listenerAdapter);
				}
			}
		}
	}

	public void unsubscribe(Object listener) {
		synchronized (listeners) {
			if (listeners.containsKey(listener)) {
				delegate.unsubscribe(listeners.remove(listener));
			}
		}
	}

	public void unsubscribeAll() {
		synchronized (listeners) {
			listeners.clear();
			delegate.unsubscribeAll();
		}
	}

}
