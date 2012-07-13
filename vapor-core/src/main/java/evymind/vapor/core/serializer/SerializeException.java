package evymind.vapor.core.serializer;

public class SerializeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SerializeException() {
		super();
	}

	public SerializeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerializeException(String message) {
		super(message);
	}

	public SerializeException(Throwable cause) {
		super(cause);
	}

}
