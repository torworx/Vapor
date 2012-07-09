package evymind.vapor.core.supertcp;

public enum SCCommand {
	/**
	 * <pre>
	 * Request or Event Id: Int32; (Event < 0; Request/Response > 0) 
	 * Length: Int32; 
	 * Data: [...] 
	 * </pre>
	 */
	CMD_PACKAGE(1),
	
	/**
	 * <pre>
	 * Id: Int32;
	 * </pre>
	 */
	CMD_ACK(2),
	
	/**
	 * <pre>
	 * Id: Int32;
	 * Error: 
	 *   0 = Message too large
	 *   1 = Timeout
	 * </pre>
	 */
	CMD_NO_ACK(3),
	
	/**
	 * <pre>
	 * Should be sent every 60 seconds by the client
	 * RandomNumber: Int32
	 * </pre>
	 */
	CMD_PING(4),
	
	/**
	 * <pre>
	 * Reply to ping
	 * OriginalRandomNumber: Int32;
	 * </pre>
	 */
	CMD_PONG(5),
	
	/**
	 * <pre>
	 * Length: Long
	 * Data: [...]
	 * UTF8 encoded, seperated by #13
	 * </pre>
	 */
	CMD_OPTIONS(6);
	
	private final byte code;
	
	private SCCommand(byte code) {
		this.code = code;
	}

	private SCCommand(int code) {
		this.code = (byte) code;
	}

	public byte code() {
		return code;
	}
	
	public static SCCommand fromCode(byte code) {
		for (SCCommand cmd : values()) {
			if (cmd.code() == code) {
				return cmd;
			}
		}
		return null;
	}
	
}
