package evymind.vapor.core.event.component;

import java.util.EventListener;

public abstract class EventMulticasterWrapper implements EventMulticaster {
	
	private EventMulticaster eventMulticaster;

	public EventMulticaster getEventMulticaster() {
		if (eventMulticaster == null) {
			eventMulticaster = new SimpleEventMulticaster();
		}
		return eventMulticaster;
	}

	public void setEventMulticaster(EventMulticaster eventMulticaster) {
		this.eventMulticaster = eventMulticaster;
	}

	public EventListener[] getListeners() {
		return getEventMulticaster().getListeners();
	}

	public void addListener(EventListener listener) {
		getEventMulticaster().addListener(listener);
	}

	public void removeListener(EventListener listener) {
		getEventMulticaster().removeListener(listener);
	}

	public void removeAllListeners() {
		getEventMulticaster().removeAllListeners();
	}

	public void setListeners(EventListener[] listeners) {
		getEventMulticaster().setListeners(listeners);
	}

	public void multicastEvent(Object event) {
		getEventMulticaster().multicastEvent(event);
	}

}
