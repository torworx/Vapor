package evymind.vapor.event.handling.annotation;

import org.junit.Test;

import evymind.vapor.core.event.SimpleEvent;
import evymind.vapor.core.event.handling.EventBus;
import evymind.vapor.core.event.handling.SimpleEventBus;
import evymind.vapor.core.event.handling.annontation.AnnotationEventListenerAdapter;
import evymind.vapor.core.event.handling.annontation.EventHandler;

public class AnnotationEventListenerTest {
	
	@Test
	public void testSimple() {
		EventBus eventBus = new SimpleEventBus();
		AnnotationEventListenerAdapter.subscribe(new StubListener(), eventBus);
		eventBus.publish(new SimpleEvent<StubEvent>(new StubEvent("Hello World!")));
	}
	
	public class StubEvent {
		
		private String message;

		public StubEvent(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

	}
	
	public class StubListener {
		
		@EventHandler
		public void handle(StubEvent event) {
			System.out.println(event.getMessage());
		}

	}

}
