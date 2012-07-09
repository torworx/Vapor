package evymind.vapor.client.proxy;

public class ProxyException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ProxyException() {
		super();
	}

	public ProxyException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProxyException(String message) {
		super(message);
	}

	public ProxyException(Throwable cause) {
		super(cause);
	}

}
