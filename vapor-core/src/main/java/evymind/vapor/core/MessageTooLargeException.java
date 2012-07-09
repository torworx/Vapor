package evymind.vapor.core;

public class MessageTooLargeException extends RemotingException {

	private static final long serialVersionUID = 1L;

	public MessageTooLargeException() {
	}

	public MessageTooLargeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MessageTooLargeException(String message) {
		super(message);
	}

	public MessageTooLargeException(Throwable cause) {
		super(cause);
	}

}
