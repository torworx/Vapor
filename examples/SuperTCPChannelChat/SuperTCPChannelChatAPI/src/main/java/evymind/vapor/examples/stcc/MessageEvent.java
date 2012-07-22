package evymind.vapor.examples.stcc;

public class MessageEvent {

	private final String from;
	private final String target;
	private final String message;

	public MessageEvent(String from, String target, String message) {
		super();
		this.from = from;
		this.target = target;
		this.message = message;
	}

	public String getFrom() {
		return from;
	}

	public String getTarget() {
		return target;
	}

	public String getMessage() {
		return message;
	}

}
