package evymind.vapor.service.impl;

import java.util.Date;
import java.util.TimerTask;
import java.util.UUID;

import evymind.vapor.core.utils.UuidUtils;
import evymind.vapor.server.eventrepository.EventRepository;
import evymind.vapor.service.api.TimeEvent;

public class TimePublishTimerTask extends TimerTask {

	private EventRepository eventRepository;
	private UUID clientId;

	public TimePublishTimerTask(EventRepository eventRepository, UUID clientId) {
		this.eventRepository = eventRepository;
		this.clientId = clientId;
	}

	@Override
	public void run() {
		eventRepository.publish(new TimeEvent(new Date()), UuidUtils.EMPTY_UUID, clientId);
	}

}
