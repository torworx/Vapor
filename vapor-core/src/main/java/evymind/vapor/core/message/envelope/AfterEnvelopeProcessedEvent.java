package evymind.vapor.core.message.envelope;

import evymind.vapor.core.Message;
import evymind.vapor.core.VaporBuffer;

public class AfterEnvelopeProcessedEvent extends AbstractEnvelopeProcessedEvent {

	protected AfterEnvelopeProcessedEvent(MessageEnvelope messageEnvelope, VaporBuffer buffer, Message message,
			MessageEnvelopeMode mode) {
		super(messageEnvelope, buffer, message, mode);
	}

}
