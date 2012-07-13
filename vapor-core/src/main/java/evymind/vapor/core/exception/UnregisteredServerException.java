package evymind.vapor.core.exception;

import evymind.vapor.core.VaporRuntimeException;

public class UnregisteredServerException extends VaporRuntimeException {

	private static final long serialVersionUID = 1L;

	public UnregisteredServerException(String exceptionName, String message) {
		super("[" + exceptionName + "] : " + message);
	}
	
}
