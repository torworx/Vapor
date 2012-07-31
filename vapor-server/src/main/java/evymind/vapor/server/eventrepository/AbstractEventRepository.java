package evymind.vapor.server.eventrepository;

import java.util.Collection;
import java.util.UUID;

import com.google.common.collect.Lists;

import evymind.vapor.core.AbstractMessageFactoryAware;
import evymind.vapor.server.ActiveEventDispatcher;

public abstract class AbstractEventRepository extends AbstractMessageFactoryAware implements EventRepository {

	@Override
	public void register(UUID clientId, ActiveEventDispatcher eventDispatcher) {
		doRegister(clientId, eventDispatcher);
	}

	@Override
	public void unregister(UUID clientId) {
		doUnregister(clientId);
	}

	@Override
	public void publish(Object event, UUID source, Collection<?> destinations) {
		doPublish(source, event, destinations);
	}
	
	@Override
	public void publish(Object event, UUID source, UUID... destinations) {
		doPublish(source, event, destinations.length == 0 ? null : Lists.newArrayList(destinations));
	}

	protected abstract void doRegister(UUID clientId, ActiveEventDispatcher eventDispatcher);

	protected abstract void doUnregister(UUID clientId);

	protected abstract void doPublish(UUID source, Object event, Collection<?> destinations);

}
