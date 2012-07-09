package evymind.vapor.core.exception;

import evymind.vapor.core.RemotingException;

public class UnregisteredServerException extends RemotingException {

	private static final long serialVersionUID = 1L;

	public UnregisteredServerException(String exceptionName, String message) {
		super("[" + exceptionName + "] : " + message);
	}
	
}
