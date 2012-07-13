package evymind.vapor.core;

public class QueueFullException extends VaporRuntimeException {

	private static final long serialVersionUID = 1L;

	public QueueFullException() {
	}

	public QueueFullException(String message, Throwable cause) {
		super(message, cause);
	}

	public QueueFullException(String message) {
		super(message);
	}

	public QueueFullException(Throwable cause) {
		super(cause);
	}

}
