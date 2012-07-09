package evymind.vapor.core.message.envelope;

import evymind.vapor.core.Message;
import evymind.vapor.core.VaporBuffer;

public class BeforeEnvelopeProcessedEvent extends AbstractEnvelopeProcessedEvent {

	protected BeforeEnvelopeProcessedEvent(MessageEnvelope messageEnvelope, VaporBuffer buffer, Message message,
			MessageEnvelopeMode mode) {
		super(messageEnvelope, buffer, message, mode);
	}

}
