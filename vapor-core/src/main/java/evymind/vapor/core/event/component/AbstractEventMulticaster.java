package evymind.vapor.core.event.component;

import java.util.EventListener;

import evymind.vapor.core.event.handling.GenericEventBus;

public abstract class AbstractEventMulticaster implements EventMulticaster {
	
	protected abstract GenericEventBus getGenericEventBus();

	@Override
	public EventListener[] getListeners() {
		Object[] originListeners = getGenericEventBus().getOriginListeners();
		EventListener[] answer = new EventListener[originListeners.length];
		System.arraycopy(originListeners, 0, answer, 0, originListeners.length);
		return answer;
	}

	@Override
	public void addListener(EventListener listener) {
		getGenericEventBus().subscribe(listener);
	}

	@Override
	public void removeListener(EventListener listener) {
		getGenericEventBus().unsubscribe(listener);
	}

	@Override
	public void removeAllListeners() {
		getGenericEventBus().unsubscribeAll();
	}

	@Override
	public void setListeners(EventListener[] listeners) {
		removeAllListeners();
		if (listeners != null) {
			for (EventListener listener : listeners) {
				addListener(listener);
			}
		}
	}

	@Override
	public void multicastEvent(Object event) {
		getGenericEventBus().publish(event);
	}

}
