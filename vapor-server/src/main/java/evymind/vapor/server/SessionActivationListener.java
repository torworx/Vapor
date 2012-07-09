package evymind.vapor.server;

import java.util.EventListener;

/**
 * Objects that are bound to a session may listen to container events notifying
 * them that sessions will be passivated and that session will be activated. A
 * container that migrates session between VMs or persists sessions is required
 * to notify all attributes bound to sessions implementing
 * SessionActivationListener.
 */
public interface SessionActivationListener extends EventListener {

	/** Notification that the session is about to be passivated. */
	public void sessionWillPassivate(SessionEvent se);

	/** Notification that the session has just been activated. */
	public void sessionDidActivate(SessionEvent se);
}
