package evymind.vapor.server;

import java.util.EventListener;

import evymind.vapor.core.utils.component.Lifecycle;

public interface SessionManager extends Lifecycle {
	
	Session getSession(String id);
	
	Session createSession(String id);
	
    /* ------------------------------------------------------------ */
    /**
     * @return the max period of inactivity, after which the session is invalidated, in seconds.
     * @see #setMaxInactiveInterval(int)
     */
    public int getMaxInactiveInterval();

    /* ------------------------------------------------------------ */
    /**
     * Sets the max period of inactivity, after which the session is invalidated, in seconds.
     *
     * @param seconds the max inactivity period, in seconds.
     * @see #getMaxInactiveInterval()
     */
    public void setMaxInactiveInterval(int seconds);

    /* ------------------------------------------------------------ */
    /**
     * Adds an event listener for session-related events.
     *
     * @param listener the session event listener to add
     *                 Individual SessionManagers implementations may accept arbitrary listener types,
     *                 but they are expected to at least handle SessionActivationListener,
     *                 SessionAttributeListener, SessionBindingListener and SessionListener.
     * @see #removeEventListener(EventListener)
     */
    public void addEventListener(EventListener listener);

    /* ------------------------------------------------------------ */
    /**
     * Removes an event listener for for session-related events.
     *
     * @param listener the session event listener to remove
     * @see #addEventListener(EventListener)
     */
    public void removeEventListener(EventListener listener);

    /* ------------------------------------------------------------ */
    /**
     * Removes all event listeners for session-related events.
     *
     * @see #removeEventListener(EventListener)
     */
    public void clearEventListeners();
    
    /* ------------------------------------------------------------ */
    /**
     * @param session the session to test for validity
     * @return whether the given session is valid, that is, it has not been invalidated.
     */
    public boolean isValid(Session session);
    

    /* ------------------------------------------------------------ */
    /**
     * Called by the {@link SessionHandler} when a session is last accessed by a request.
     *
     * @param session the session object
     * @see #access(Session, boolean)
     */
    public void complete(Session session);


}
