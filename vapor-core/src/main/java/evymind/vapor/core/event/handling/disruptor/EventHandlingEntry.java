package evymind.vapor.core.event.handling.disruptor;

import com.lmax.disruptor.EventFactory;

import evymind.vapor.core.event.Event;

/**
 * DataHolder for the DisruptorEventBus. The EventHandlingEntry maintains all information required for or produced
 * by the event handling process.
 *
 * @author Tao Yuan
 * @since 0.1
 */
public class EventHandlingEntry {
	
	private Object[] events;
	
	public Object[] getEvents() {
		return events;
	}
	
	public void reset(Object... events) {
		this.events = events;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(EventHandlingEntry.class.getSimpleName()).append("-{");
		if (events != null) {
			sb.append("events: [");
			for (int i = 0; i < events.length; i++) {
				Object object = events[i];
				if (object instanceof Event) {
					sb.append(((Event<?>)object).getPayloadType().getSimpleName());
				} else {
					sb.append(object.getClass().getSimpleName());
				}
				if (i < events.length - 1) {
					sb.append(", ");
				}
			}
			sb.append("]");
		}
		sb.append("}");
		return sb.toString();
	}

    /**
     * Factory class for EventHandlingEntry instances.
     */
	public static class Factory implements EventFactory<EventHandlingEntry> {

		@Override
		public EventHandlingEntry newInstance() {
			return new EventHandlingEntry();
		}
		
	}
}
