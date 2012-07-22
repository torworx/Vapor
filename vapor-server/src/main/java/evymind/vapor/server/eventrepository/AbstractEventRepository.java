package evymind.vapor.server.eventrepository;

import java.util.Collection;
import java.util.UUID;

import com.google.common.collect.Lists;

import evymind.vapor.core.AbstractMessageFactoryAware;
import evymind.vapor.server.ActiveEventDispatcher;

public abstract class AbstractEventRepository extends AbstractMessageFactoryAware implements EventRepository {

	@Override
	public void register(UUID clientId, ActiveEventDispatcher eventDispatcher) {
		doSubscribe(clientId, eventDispatcher);
	}

	@Override
	public void unregister(UUID clientId) {
		doUnsubscribe(clientId);
	}

	@Override
	public void publish(Object event, UUID source, Collection<?> destinations) {
		doPublish(source, event, destinations);
	}
	
	@Override
	public void publish(Object event, UUID source, UUID... destinations) {
		doPublish(source, event, destinations.length == 0 ? null : Lists.newArrayList(destinations));
	}

	protected abstract void doSubscribe(UUID clientId, ActiveEventDispatcher eventDispatcher);

	protected abstract void doUnsubscribe(UUID clientId);

	protected abstract void doPublish(UUID source, Object event, Collection<?> destinations);

}
