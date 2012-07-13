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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
		result = prime * result + ((listeners == null) ? 0 : listeners.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenericEventBus other = (GenericEventBus) obj;
		if (delegate == null) {
			if (other.delegate != null)
				return false;
		} else if (!delegate.equals(other.delegate))
			return false;
		if (listeners == null) {
			if (other.listeners != null)
				return false;
		} else if (!listeners.equals(other.listeners))
			return false;
		return true;
	}

}
