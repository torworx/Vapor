package evymind.vapor.examples.stcc.impl;

import evymind.vapor.examples.stcc.LoginService;
import evymind.vapor.examples.stcc.UserLoginEvent;
import evymind.vapor.examples.stcc.UserLogoutEvent;
import evymind.vapor.server.ActiveEventDispatcher;
import evymind.vapor.server.RequestHolder;
import evymind.vapor.server.ServiceRequest;
import evymind.vapor.server.Session;
import evymind.vapor.server.eventrepository.EventRepository;

public class LoginServiceImpl implements LoginService {
	
	@Override
	public synchronized void login(String nickname) {
		ServiceRequest request = RequestHolder.getServiceRequest();
		Session session = request.getSession(true);
		String s = session.getAttribute("nick");
		if (s != null) {
			Chats.USERS.remove(s);
		}
		
		EventRepository eventRepository = request.getConnector().getEventRepository();
		eventRepository.register(request.getClientId(), (ActiveEventDispatcher) request.getTransport());
		
		// Publish other users login event to current user
		eventRepository.publish(new UserLoginEvent(Chats.USERS.keySet()), request.getClientId(), request.getClientId());
		
		Chats.USERS.put(nickname, request.getClientId());
		
		// Publish current user login event to other users
		eventRepository.publish(new UserLoginEvent(nickname), request.getClientId());
		
		session.setAttribute("nick", nickname);
	}

	@Override
	public synchronized void logout() {
		ServiceRequest request = RequestHolder.getServiceRequest();
		Session session = request.getSession(true);
		String s = session.getAttribute("nick");
		
		if (Chats.USERS.containsKey(s)) {
			Chats.USERS.remove(s);
		}
		
		EventRepository eventRepository = request.getConnector().getEventRepository();
		eventRepository.publish(new UserLogoutEvent(s), request.getClientId());
		eventRepository.unregister(request.getClientId());
	}
	
}
