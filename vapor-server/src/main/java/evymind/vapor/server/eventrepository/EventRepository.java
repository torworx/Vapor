package evymind.vapor.server.eventrepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import evymind.vapor.core.VaporBuffer;
import evymind.vapor.server.ActiveEventDispatcher;

public interface EventRepository {
	
	void register(UUID clientId, ActiveEventDispatcher eventDispatcher);
	
	void unregister(UUID clientId);
	
	void publish(Object event, UUID source, List<?> destinations);
	
	void publish(Object event, UUID source, UUID... destinations);
	
	Collection<VaporBuffer> getStoredEvents(UUID clientId);
}
