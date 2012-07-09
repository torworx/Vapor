package evymind.vapor.app.echo;

import java.util.EventListener;

import evymind.vapor.core.event.handling.annontation.EventHandler;
import evymind.vapor.server.event.ClientConnectedEvent;

public class EchoListener implements EventListener {
	
	@EventHandler
	public void handleConnectedEvent(ClientConnectedEvent event) {
		// nop
	}

}
