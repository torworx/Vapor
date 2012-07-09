package evymind.vapor.core.event.handling;

import evymind.vapor.core.event.Event;

@SuppressWarnings("rawtypes")
public class RunnableEventListener implements EventListener {
	
	public static final RunnableEventListener INSTANCE = new RunnableEventListener();
	
	private RunnableEventListener(){
	}
	
	@Override
	public void handle(Event event) {
		Object payload = event.getPayload();
		if (payload instanceof Runnable) {
			((Runnable) payload).run();
		}
	}

}
