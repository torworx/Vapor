package evymind.vapor.server;

@SuppressWarnings("serial")
public class SessionBindingEvent extends SessionEvent {

	/* The name to which the object is being bound or unbound */
	private String name;

	/* The object is being bound or unbound */
	private Object value;

	/**
	 * 
	 * Constructs an event that notifies an object that it has been bound to or unbound from a session. To receive the
	 * event, the object must implement {@link HttpSessionBindingListener}.
	 * 
	 * @param session
	 *            the session to which the object is bound or unbound
	 * @param name
	 *            the name with which the object is bound or unbound
	 * 
	 * @see #getName
	 * @see #getSession
	 * 
	 */

	public SessionBindingEvent(Session session, String name) {
		super(session);
		this.name = name;
	}

	/**
	 * 
	 * Constructs an event that notifies an object that it has been bound to or unbound from a session. To receive the
	 * event, the object must implement {@link HttpSessionBindingListener}.
	 * 
	 * @param session
	 *            the session to which the object is bound or unbound
	 * @param name
	 *            the name with which the object is bound or unbound
	 * 
	 * @see #getName
	 * @see #getSession
	 * 
	 */

	public SessionBindingEvent(Session session, String name, Object value) {
		super(session);
		this.name = name;
		this.value = value;
	}

	/** Return the session that changed. */
	public Session getSession() {
		return super.getSession();
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return this.value;
	}

}
