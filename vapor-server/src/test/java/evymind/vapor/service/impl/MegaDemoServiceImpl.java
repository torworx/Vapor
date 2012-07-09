package evymind.vapor.service.impl;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.google.common.collect.Maps;

import evymind.vapor.core.event.handling.annontation.EventHandler;
import evymind.vapor.server.ActiveEventDispatcher;
import evymind.vapor.server.RequestHolder;
import evymind.vapor.server.ServiceRequest;
import evymind.vapor.server.Session;
import evymind.vapor.server.event.ClientConnectedEvent;
import evymind.vapor.server.event.ClientDisconnectedEvent;
import evymind.vapor.server.eventrepository.EventRepository;
import evymind.vapor.service.api.MegaDemoService;

public class MegaDemoServiceImpl implements MegaDemoService, EventListener {
	
	private Timer timer = new Timer();
	private Map<Object, TimerTask> tasks = Maps.newHashMap();

	@Override
	public int sum(int a, int b) {
		return a + b;
	}

	@Override
	public List<String> getList(String a, String b) {
		List<String> answer = new ArrayList<String>();
		answer.add(a);
		answer.add(b);
		return answer;
	}

	@Override
	public String echo(String message) {
		return message;
	}

	@Override
	public String getAddressFromRequest() {
		ServiceRequest request = RequestHolder.getServiceRequest();
		return request.getRemoteAddress() + ":" + request.getRemotePort();
	}

	@Override
	public void setSessionValue(String name, Object value) {
		ServiceRequest request = RequestHolder.getServiceRequest();
		Session session = request.getSession(true);
		session.setAttribute(name, value);
	}

	@Override
	public Object getSessionValue(String name) {
		ServiceRequest request = RequestHolder.getServiceRequest();
		Session session = request.getSession(true);
		return session.getAttribute(name);
	}

	@Override
	public void subscribeTime(int interval) {
		ServiceRequest request = RequestHolder.getServiceRequest();
		UUID clientId = request.getClientId();
		cancelTask(clientId);
		EventRepository eventRepository = request.getConnector().getEventRepository();
		if (eventRepository != null) {
			eventRepository.register(request.getClientId(), (ActiveEventDispatcher) request.getTransport());
			TimerTask task = new TimePublishTimerTask(eventRepository, request.getClientId());
			tasks.put(clientId, task);
			timer.schedule(task, interval, interval);
		}
	}
	
	protected void cancelTask(UUID clientId) {
		TimerTask task = tasks.get(clientId);
		if (task != null) {
			task.cancel();
			tasks.remove(clientId);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	// ServiceServerListener
	
	@EventHandler
	public void handleClientConnected(ClientConnectedEvent event) {
		System.out.println(event);
	}

	@EventHandler
	public void handleClientDisconnected(ClientDisconnectedEvent event) {
		cancelTask(event.getClientId());
		System.out.println(event);
	}

}
