package evymind.vapor.core.supertcp;

import evymind.vapor.core.Signal;

public interface PackageAck {
	
	static final byte NOACK_MESSAGE_TOO_LARGE = 0;
	static final byte NOACK_TIMEOUT = 1;
	static final byte NOACK_UNKNOWN_COMMAND = 2;
	static final byte NOACK_QUEUE_FULL = 3;
	
	static final byte NOACK_SUPPORTS_OPTIONS = (byte) 255;

	int getAckNo();
	
	AckState getAckState();
	
	void setAckState(AckState state);
	
	int getAckError();
	
	void setAckError(int noAckError);
	
	Signal getSignal();
}
