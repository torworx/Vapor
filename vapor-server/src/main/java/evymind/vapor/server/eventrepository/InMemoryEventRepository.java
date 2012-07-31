package evymind.vapor.server.eventrepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import evymind.vapor.core.Message;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.VaporBuffers;
import evymind.vapor.server.ActiveEventDispatcher;
import evymind.vapor.server.supertcp.TransportInvalidException;

public class InMemoryEventRepository extends AbstractEventRepository {

	private static final Logger log = LoggerFactory.getLogger(InMemoryEventRepository.class);

	private final Map<UUID, ClientReference> clients = Maps.newLinkedHashMap();
	private final ReentrantLock lock = new ReentrantLock();

	public InMemoryEventRepository() {
		super();
	}
	
	protected ClientReference getClientReference(UUID clientId) {
		ClientReference answer = clients.get(clientId);
		if (answer == null) {
			answer = new ClientReference(clientId);
			clients.put(clientId, answer);
		}
		return answer;
	}

	@Override
	protected void doRegister(UUID clientId, ActiveEventDispatcher eventDispatcher) {
		lock.lock();
		try {
			if (eventDispatcher != null) {
				log.debug("Subscribe event for {}", clientId);
				ClientReference clientReference = getClientReference(clientId);
				if (clientReference.getDispatcher() != null) {
					log.warn("Replace dispatcher [{}] wiht [{}] for [{}]", new Object[]{clientReference.getDispatcher(), eventDispatcher, clientId});
				}
				clientReference.setDispatcher(eventDispatcher);
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	protected void doUnregister(UUID clientId) {
		lock.lock();
		try {
			if (clients. containsKey(clientId)) {
				log.debug("Unsubscribe event for " + clientId);
				ClientReference clientReference = clients.remove(clientId);
				if (clientReference != null) {
					clientReference.setDispatcher(null);
					clientReference.clearEvents();
				}
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	protected void doPublish(UUID source, Object event, Collection<?> destinations) {
		if ((destinations == null || destinations.isEmpty()) && clients.isEmpty()) {
			return;
		}
		
		VaporBuffer eventData;
		if (!(event instanceof VaporBuffer)) {
			eventData = VaporBuffers.dynamicBuffer();

			Message message = getMessageFactory().createMessage();
			message.initializeEventMessage(null, "", "", event.getClass().getName());
			message.writeObject("event", event);
			message.finalizeMessage();

			message.writeToBuffer(eventData);
		} else {
			eventData = (VaporBuffer) event;
		}
		doPublish(source, eventData, destinations);
	}

	private void doPublish(UUID source, VaporBuffer eventData, Collection<?> destinations) {

		lock.lock();
		try {
			Object[] destinationsToProcess = null;
			if (destinations != null && !destinations.isEmpty()) {
				destinationsToProcess = destinations.toArray();
			}
			if (destinationsToProcess == null) {
				destinationsToProcess = clients.keySet().toArray();
			}
			
			log.debug("Publish event [{}] from {} to {}", new Object[] { eventData, source, destinationsToProcess });
			for (Object destination : destinationsToProcess) {
				UUID clientId;
				if (destination instanceof UUID) {
					clientId = (UUID) destination;
				} else if (destination instanceof String) {
					clientId = UUID.fromString((String) destination);
				} else {
					continue;
				}
				
				if (!clients.containsKey(destination)) {
					continue;
				}
				
				ClientReference clientReference = clients.get(clientId);
				try {
					if ((clientReference.getDispatcher() != null)) {
						log.debug("Dispatching event to {}", clientId);
						clientReference.getDispatcher().dispatchEvent(clientId, eventData);
					} else {
						log.debug("Store event in memory for {}", clientId);
						clientReference.addEvent(eventData);
					}
				} catch (TransportInvalidException e) {
					doUnregister(clientId);
				}
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Collection<VaporBuffer> getStoredEvents(UUID clientId) {
		if (clients.containsKey(clientId)) {
			return clients.get(clientId).getEvents();
		}
		return null;
	}

	class ClientReference {
		
		private final UUID clientId;
		private final List<VaporBuffer> events;
		
		private ActiveEventDispatcher dispatcher;

		public ClientReference(UUID clientId) {
			this.clientId = clientId;
			events = Lists.newArrayList();
		}

		public UUID getClientId() {
			return clientId;
		}
		
		public ActiveEventDispatcher getDispatcher() {
			return dispatcher;
		}

		public void setDispatcher(ActiveEventDispatcher dispatcher) {
			this.dispatcher = dispatcher;
		}

		public boolean hasEvents() {
			return !events.isEmpty();
		}

		public Collection<VaporBuffer> getEvents() {
			return Collections.unmodifiableCollection(events);
		}

		public void addEvent(VaporBuffer eventData) {
			events.add(eventData);
		}

		public void clearEvents() {
			events.clear();
		}

	}
}
