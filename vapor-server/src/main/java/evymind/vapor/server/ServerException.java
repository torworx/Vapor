package evymind.vapor.server;

import evymind.vapor.core.VaporRuntimeException;

public class ServerException extends VaporRuntimeException {

	private static final long serialVersionUID = 1L;

	public ServerException() {
		super();
	}

	public ServerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerException(String message) {
		super(message);
	}

	public ServerException(Throwable cause) {
		super(cause);
	}

}
