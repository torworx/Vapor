package evymind.vapor.examples.stcc.impl;

import java.util.EventListener;
import java.util.UUID;
import java.util.Map.Entry;

import evymind.vapor.core.event.handling.annontation.EventHandler;
import evymind.vapor.server.event.ClientDisconnectedEvent;

public class LoginListener implements EventListener {

	@EventHandler
	public synchronized void handleClientDisconnectedEvent(ClientDisconnectedEvent event) {
		UUID clientId = event.getClientId();
		if (Chats.USERS.containsValue(clientId)) {
			String nickname = null;
			for (Entry<String, UUID> entry : Chats.USERS.entrySet()) {
				if (clientId.equals(entry.getValue())) {
					nickname = entry.getKey();
					break;
				}
			}
			if (nickname != null) {
				Chats.USERS.remove(nickname);
			}
		}
	}
}
