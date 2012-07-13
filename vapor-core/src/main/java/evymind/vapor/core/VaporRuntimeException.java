package evymind.vapor.core;

public class VaporRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public VaporRuntimeException() {
		super();
	}

	public VaporRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public VaporRuntimeException(String message) {
		super(message);
	}

	public VaporRuntimeException(Throwable cause) {
		super(cause);
	}

}
