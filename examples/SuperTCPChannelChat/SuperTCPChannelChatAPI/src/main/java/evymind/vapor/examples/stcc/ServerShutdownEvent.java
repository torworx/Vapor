package evymind.vapor.examples.stcc;

public class ServerShutdownEvent {
	
	private final String cause;

	public ServerShutdownEvent() {
		this("Maintance shutdown");
	}

	public ServerShutdownEvent(String cause) {
		this.cause = cause;
	}

	public String getCause() {
		return cause;
	}

}
