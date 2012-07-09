package evymind.vapor.core.message;

public enum MessageType {
    REQUEST(0),
    RESPONSE(1),
    ASYNC_MESSAGE(2),
    EXCEPTION(3),
    QUERY_SERVICE_INFO(4),
    EVENT(5), 
    POLL(6),
    POLL_RESPONSE(7),
    ASYNC_ACK(8);
	
	private final byte code;
	
	MessageType(int code) {
		this.code = (byte) code;
	}
	
	public byte getCode() {
		return code;
	}
	
}
