package evymind.vapor.server;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;

import evyframework.common.Assert;
import evyframework.common.CollectionUtils;
import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.Transport;

public class MessageDispatchers {
	
	private ServerHolder serverHolder;
	private final Set<MessageDispatcher> dispatchersList = Sets.newCopyOnWriteArraySet();
	
	public MessageDispatchers(ServerHolder serverHolder) {
		this.serverHolder = serverHolder;
	}

	public MessageDispatchers(Server server) {
		setServer(server);
	}

	public Server getServer() {
		return serverHolder.getServer();
	}

	public void setServer(Server server) {
		this.serverHolder = new SimpleServerHoler(server);
	}

	public MessageDispatcher findDispatcher(Transport transport, Request request) {
		for (MessageDispatcher dispatcher : dispatchersList) {
			if (dispatcher.canHandleMessage(transport, request)) {
				return dispatcher;
			}
		}
		return null;
	}
	
	public MessageDispatcher addDispatcher(MessageFactory<?> messageFactory) {
		MessageDispatcher dispatcher = new MessageDispatcher(this, messageFactory);
		dispatchersList.add(dispatcher);
		return dispatcher;
	}
	
	public void addDispatcher(MessageDispatcher dispatcher) {
		Assert.notNull(dispatcher, "'dispatcher' must not be null");
		dispatcher.setDispatchers(this);
	}
	
	protected void doAddDispatcher(MessageDispatcher dispatcher) {
		Assert.notNull(dispatcher, "'dispatcher' must not be null");
		if (!dispatchersList.contains(dispatcher)) {
			dispatchersList.add(dispatcher);
		}
	}
	
	public void removeDispathcer(MessageDispatcher dispatcher) {
		Assert.notNull(dispatcher, "'dispatcher' must not be null");
		if (Objects.equal(this, dispatcher.getDispatchers())) {
			dispatcher.setDispatchers(null);
		}
	}
	
	protected void doRemoveDispatcher(MessageDispatcher dispatcher) {
		Assert.notNull(dispatcher, "'dispatcher' must not be null");
		dispatchersList.remove(dispatcher);
	}
	
	public Collection<MessageDispatcher> getDispatchers() {
		return Collections.unmodifiableSet(dispatchersList);
	}

	public void setDispatchers(Collection<MessageDispatcher> dispatchers) {
		this.dispatchersList.clear();
		if (!CollectionUtils.isEmpty(dispatchers)) {
			for (MessageDispatcher dispatcher : dispatchers) {
				addDispatcher(dispatcher);
			}
		}
	}

	class SimpleServerHoler implements ServerHolder {
		
		private final Server server;

		private SimpleServerHoler(Server server) {
			this.server = server;
		}

		@Override
		public Server getServer() {
			return server;
		}
		
	}
}
