package evymind.vapor.core;


public class AbstractMessageFactoryAware implements MessageFatoryAware {
	
	private MessageFactory<?> messageFactory;

	@Override
	public MessageFactory<?> getMessageFactory() {
		return messageFactory;
	}

	@Override
	public void setMessageFactory(MessageFactory<?> factory) {
		this.messageFactory = factory;
	}

}
