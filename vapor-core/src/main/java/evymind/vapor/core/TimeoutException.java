package evymind.vapor.core;

public class TimeoutException extends RemotingException {

	private static final long serialVersionUID = 1L;

	public TimeoutException() {
	}

	public TimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimeoutException(String message) {
		super(message);
	}

	public TimeoutException(Throwable cause) {
		super(cause);
	}

}
