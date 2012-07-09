package evymind.vapor.core;


public interface MessageFatoryAware {
	
	MessageFactory<?> getMessageFactory();
	
	void setMessageFactory(MessageFactory<?> factory);

}
