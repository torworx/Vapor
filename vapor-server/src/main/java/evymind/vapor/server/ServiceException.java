package evymind.vapor.server;

public class ServiceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ServiceException() {
		// TODO Auto-generated constructor stub
	}

	public ServiceException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ServiceException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
