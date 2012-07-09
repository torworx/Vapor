package evymind.vapor.server;

import com.google.common.base.Objects;

import evymind.vapor.core.Message;
import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.Transport;

public class MessageDispatcher {
	
	private MessageDispatchers dispatchers;
	private boolean enabled = true;
	private MessageFactory<?> messageFactory;
	
	public MessageDispatcher() {
	}

	public MessageDispatcher(MessageFactory<?> messageFactory) {
		this(null, messageFactory);
	}

	public MessageDispatcher(MessageDispatchers dispatchers, MessageFactory<?> messageFactory) {
		this.messageFactory = messageFactory;
		if (dispatchers != null) {
			setDispatchers(dispatchers);
		}
	}

	public MessageDispatchers getDispatchers() {
		return dispatchers;
	}

	public void setDispatchers(MessageDispatchers dispatchers) {
		if (!Objects.equal(this, dispatchers)) {
			if (this.dispatchers != null) {
				this.dispatchers.doRemoveDispatcher(this);
			}
			this.dispatchers = dispatchers;
			if (this.dispatchers != null) {
				this.dispatchers.doAddDispatcher(this);
			}
		}
	}

	public boolean canHandleMessage(final Transport transport, Request request) {
		return isEnabled();
	}
	
	public void processMessage(Transport transport, Request request, Response response) {
		Message message = messageFactory.createMessage();
		request.setMessage(message);
		response.setMessage(message);
		dispatchers.getServer().handle(transport, request, response);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public MessageFactory<?> getMessageFactory() {
		return messageFactory;
	}

	public void setMessageFactory(MessageFactory<?> messageFactory) {
		this.messageFactory = messageFactory;
	}
	
}
