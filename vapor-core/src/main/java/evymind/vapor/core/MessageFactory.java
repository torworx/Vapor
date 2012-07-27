package evymind.vapor.core;

import evymind.vapor.core.message.envelope.MessageEnvelope;
import evymind.vapor.core.message.envelope.MessageEnvelopes;

public interface MessageFactory<T extends Message> {

    String getName();
	
	MessageEnvelopes getEnvelopes();
	
	void setEnvelopes(MessageEnvelopes envelopes);
	
	MessageFactory<T> addEnvelope(MessageEnvelope envelope);

	T createMessage();
}
