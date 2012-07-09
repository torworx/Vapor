package evymind.vapor.core.event.component;

import evymind.vapor.core.event.handling.EventBus;
import evymind.vapor.core.event.handling.GenericEventBus;

public class SimpleEventMulticaster extends AbstractEventMulticaster {
	
	private GenericEventBus genericEventBus;

	public SimpleEventMulticaster() {
		genericEventBus = new GenericEventBus();
	}

	public SimpleEventMulticaster(EventBus eventBus) {
		genericEventBus = new GenericEventBus(eventBus);
	}
	
	@Override
	protected GenericEventBus getGenericEventBus() {
		return genericEventBus;
	}

}
