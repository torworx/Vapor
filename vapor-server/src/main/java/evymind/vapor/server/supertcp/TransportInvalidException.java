package evymind.vapor.server.supertcp;

public class TransportInvalidException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public TransportInvalidException() {
	}

	public TransportInvalidException(String message) {
		super(message);
	}

	public TransportInvalidException(Throwable cause) {
		super(cause);
	}

	public TransportInvalidException(String message, Throwable cause) {
		super(message, cause);
	}

}
