package evymind.vapor.event.handling.disruptor;

import org.junit.Test;

import evymind.vapor.core.event.handling.EventBus;
import evymind.vapor.core.event.handling.annontation.AnnotationEventListenerAdapter;
import evymind.vapor.core.event.handling.annontation.EventHandler;
import evymind.vapor.core.event.handling.disruptor.DisruptorEventBus;

public class DisruptorEventBusTest {
	
	@Test
	public void testSimple() {
		EventBus eventBus = new DisruptorEventBus();
		AnnotationEventListenerAdapter.subscribe(new StubListener(), eventBus);
		eventBus.publish(new StubEvent("hello world - 1"),
				new StubEvent("hello world - 2"));
		
	}
	
	
	public class StubEvent {
		private final String message;

		public StubEvent(String message) {
			super();
			this.message = message;
		}

		public String getMessage() {
			return message;
		}
		
	}
	
	public class StubListener {
		
		@EventHandler
		public void handleStubEvent(StubEvent event) {
			System.out.println(event.getMessage());
		}
	}

}
