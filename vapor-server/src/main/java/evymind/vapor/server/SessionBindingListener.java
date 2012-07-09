package evymind.vapor.server;

import java.util.EventListener;

/**
 * Causes an object to be notified when it is bound to or unbound from a
 * session. The object is notified by an {@link SessionBindingEvent} object.
 * This may be as a result of a servlet programmer explicitly unbinding an
 * attribute from a session, due to a session being invalidated, or due to a
 * session timing out.
 * 
 * 
 * @author Various
 * 
 * @see Session
 * @see SessionBindingEvent
 * 
 */
public interface SessionBindingListener extends EventListener {

	/**
	 * 
	 * Notifies the object that it is being bound to a session and identifies
	 * the session.
	 * 
	 * @param event
	 *            the event that identifies the session
	 * 
	 * @see #valueUnbound
	 * 
	 */

	public void valueBound(SessionBindingEvent event);

	/**
	 * 
	 * Notifies the object that it is being unbound from a session and
	 * identifies the session.
	 * 
	 * @param event
	 *            the event that identifies the session
	 * 
	 * @see #valueBound
	 * 
	 */

	public void valueUnbound(SessionBindingEvent event);
}
