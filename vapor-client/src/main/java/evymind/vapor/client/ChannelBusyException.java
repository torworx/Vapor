package evymind.vapor.client;

import evymind.vapor.core.RemotingException;

public class ChannelBusyException extends RemotingException {

	private static final long serialVersionUID = 1L;

	public ChannelBusyException() {
		// TODO Auto-generated constructor stub
	}

	public ChannelBusyException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ChannelBusyException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ChannelBusyException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
