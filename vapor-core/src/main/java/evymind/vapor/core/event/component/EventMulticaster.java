package evymind.vapor.core.event.component;

import java.util.EventListener;

/**
 * Interface to be implemented by objects that can manage a number of
 * {@link EventListener} objects, and publish events to them.
 * 
 */
public interface EventMulticaster {
	
	EventListener[] getListeners();

	/**
	 * Add a listener to be notified of all events.
	 * @param listener the listener to add
	 */
	void addListener(EventListener listener);

	/**
	 * Remove a listener from the notification list.
	 * @param listener the listener to remove
	 */
	void removeListener(EventListener listener);

	/**
	 * Remove all listeners registered with this multicaster.
	 * <p>After a remove call, the multicaster will perform no action
	 * on event notification until new listeners are being registered.
	 */
	void removeAllListeners();
	
	/**
	 * Remove all listeners registered with this multicaster first, then
	 * add all listeners
	 * @param listeners the listeners to set
	 */
	void setListeners(EventListener[] listeners);
	
	/**
	 * Multicast the given application event to appropriate listeners.
	 * @param event the event to multicast
	 */
	void multicastEvent(Object event);
}
