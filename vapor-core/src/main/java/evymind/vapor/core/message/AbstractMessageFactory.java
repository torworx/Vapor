package evymind.vapor.core.message;

import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.message.envelope.MessageEnvelope;
import evymind.vapor.core.message.envelope.MessageEnvelopes;

public abstract class AbstractMessageFactory<T extends AbstractMessage<?>> implements MessageFactory<T>{
	
	private MessageEnvelopes envelopes;

    @Override
    public String getName() {
        return "Unknown";
    }

    public MessageEnvelopes getEnvelopes() {
		if (envelopes == null) {
			envelopes = new MessageEnvelopes();
		}
		return envelopes;
	}

	public void setEnvelopes(MessageEnvelopes envelopes) {
		this.envelopes = envelopes;
	}

	@Override
	public MessageFactory<T> addEnvelope(MessageEnvelope envelope) {
		getEnvelopes().addEnvelope(envelope);
		return this;
	}
	
	@Override
	public T createMessage() {
		T answer = doCreateMessage();
		if (envelopes != null) {
			answer.setEnvelopes(envelopes);
		}
		return answer;
	}
	
	
	protected abstract T doCreateMessage();

}
