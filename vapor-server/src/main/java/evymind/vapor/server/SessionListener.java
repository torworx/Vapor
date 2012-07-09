package evymind.vapor.server;

import java.util.EventListener;

public interface SessionListener extends EventListener {
	
    /** 
     * Receives notification that a session has been created.
     *
     * @param se the SessionEvent containing the session
     */
    public void sessionCreated(SessionEvent se);
    
    /** 
     * Receives notification that a session is about to be invalidated.
     *
     * @param se the SessionEvent containing the session
     */
    public void sessionDestroyed(SessionEvent se);
}
