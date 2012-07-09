package evymind.vapor.server.eventrepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import evymind.vapor.core.Message;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.Buffers;
import evymind.vapor.server.ActiveEventDispatcher;

public class InMemoryEventRepository extends AbstractEventRepository {

	private static final Logger log = LoggerFactory.getLogger(InMemoryEventRepository.class);

	private final Map<UUID, ClientReference> clients = Maps.newLinkedHashMap();

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
	protected void doSubscribe(UUID clientId, ActiveEventDispatcher eventDispatcher) {
		synchronized (clients) {
			if (eventDispatcher != null) {
				log.debug("Subscribe event for {}", clientId);
				if (!getClientReference(clientId).addDispatcher(eventDispatcher)) {
					log.debug("Already subscribed for {}", clientId);
				}
			}
		}
	}

	@Override
	protected void doUnsubscribe(UUID clientId) {
		synchronized (clients) {
			if (clients. containsKey(clientId)) {
				log.debug("Unsubscribe event for " + clientId);
				ClientReference clientReference = clients.remove(clientId);
				if (clientReference != null) {
					clientReference.clearAll();
				}
			}
		}
	}

	@Override
	protected void doPublish(UUID source, Object event, Collection<?> destinations) {
		VaporBuffer eventData;
		if (!(event instanceof VaporBuffer)) {
			eventData = Buffers.dynamicBuffer();

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

	protected void doPublish(UUID source, VaporBuffer eventData, Collection<?> destinations) {
		if (destinations == null) {
			destinations = clients.keySet();
		}
		log.debug("Publish event [{}] from {} to {}", new Object[] { eventData, source, destinations });

		synchronized (clients) {
			for (Object destination : destinations) {
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
				if ((clientReference.hasDispatchers())) {
					log.debug("Dispatching event to {}", clientId);
					for (ActiveEventDispatcher dispatcher : clientReference.getDispatchers()) {
						dispatcher.dispatchEvent(clientId, eventData);
					}
				} else {
					log.debug("Store event in memory for {}", clientId);
					clientReference.addEvent(eventData);
				}
			}
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

		private final Set<ActiveEventDispatcher> dispatchers;
		private final List<VaporBuffer> events;

		public ClientReference(UUID clientId) {
			this.clientId = clientId;
			dispatchers = Sets.newLinkedHashSet();
			events = Lists.newArrayList();
		}

		public UUID getClientId() {
			return clientId;
		}
		
		public boolean hasDispatchers() {
			return !dispatchers.isEmpty();
		}

		public Set<ActiveEventDispatcher> getDispatchers() {
			return Collections.unmodifiableSet(dispatchers);
		}

		public boolean addDispatcher(ActiveEventDispatcher dispatcher) {
			return dispatchers.add(dispatcher);
		}

		public void clearDispatchers() {
			dispatchers.clear();
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

		public void clearAll() {
			clearDispatchers();
			clearEvents();
		}
	}
}
