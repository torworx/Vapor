package evymind.vapor.core.message.envelope;

import evymind.vapor.core.Message;
import evymind.vapor.core.VaporBuffer;

public class AbstractEnvelopeProcessedEvent {
	
	private final MessageEnvelope messageEnvelope;
	
	private final VaporBuffer buffer;
	
	private final Message message;
	
	private final MessageEnvelopeMode mode;
	
	protected AbstractEnvelopeProcessedEvent(MessageEnvelope messageEnvelope, VaporBuffer buffer, Message message,
			MessageEnvelopeMode mode) {
		super();
		this.messageEnvelope = messageEnvelope;
		this.buffer = buffer;
		this.message = message;
		this.mode = mode;
	}

	public MessageEnvelope getMessageEnvelope() {
		return messageEnvelope;
	}

	public VaporBuffer getBuffer() {
		return buffer;
	}

	public Message getMessage() {
		return message;
	}

	public MessageEnvelopeMode getMode() {
		return mode;
	}

}
