package evymind.vapor.server;

import java.util.EventObject;

@SuppressWarnings("serial")
public class SessionEvent extends EventObject {

	public SessionEvent(Object source) {
		super(source);
	}

	public Session getSession() {
		return (Session) super.getSource();
	}

}
