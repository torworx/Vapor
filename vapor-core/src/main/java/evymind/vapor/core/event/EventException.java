package evymind.vapor.core.event;

public class EventException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EventException() {
	}

	public EventException(String message) {
		super(message);
	}

	public EventException(Throwable cause) {
		super(cause);
	}

	public EventException(String message, Throwable cause) {
		super(message, cause);
	}

}
