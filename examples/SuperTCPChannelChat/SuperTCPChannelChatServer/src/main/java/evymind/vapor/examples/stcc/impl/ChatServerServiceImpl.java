package evymind.vapor.examples.stcc.impl;

import java.util.UUID;

import evymind.vapor.examples.stcc.ChatServerService;
import evymind.vapor.examples.stcc.InvalidUserException;
import evymind.vapor.examples.stcc.MessageEvent;
import evymind.vapor.server.RequestHolder;
import evymind.vapor.server.ServiceRequest;
import evymind.vapor.server.Session;
import evymind.vapor.server.eventrepository.EventRepository;

public class ChatServerServiceImpl implements ChatServerService {

	@Override
	public void talk(String message) {
		ServiceRequest request = RequestHolder.getServiceRequest();
		Session session = request.getSession();
		EventRepository eventRepository = request.getConnector().getEventRepository();
		
		eventRepository.publish(new MessageEvent((String) session.getAttribute("nick"), "", message), 
				request.getClientId());
	}

	@Override
	public void talkPrivate(String targetNickname, String message) {
		ServiceRequest request = RequestHolder.getServiceRequest();
		Session session = request.getSession();
		EventRepository eventRepository = request.getConnector().getEventRepository();
		
		UUID targetClientId = Chats.USERS.get(targetNickname);
		if (targetClientId == null) {
			throw new InvalidUserException("Invalid user: " + targetNickname);
		}
		eventRepository.publish(new MessageEvent((String) session.getAttribute("nick"), targetNickname, message), 
				request.getClientId(), request.getClientId(), targetClientId);
	}

}
