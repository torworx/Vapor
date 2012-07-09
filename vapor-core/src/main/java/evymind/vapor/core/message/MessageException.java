package evymind.vapor.core.message;

public class MessageException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MessageException() {
		super();
	}

	public MessageException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public MessageException(String arg0) {
		super(arg0);
	}

	public MessageException(Throwable arg0) {
		super(arg0);
	}

}
