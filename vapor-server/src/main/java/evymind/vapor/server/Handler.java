package evymind.vapor.server;

import java.io.IOException;
import java.util.UUID;

import evymind.vapor.core.Transport;
import evymind.vapor.core.utils.component.Destroyable;
import evymind.vapor.core.utils.component.Lifecycle;

public interface Handler extends Lifecycle, Destroyable {
	
	void connected(Transport transport, UUID clientId);
	
	void disconnected(Transport transport, UUID clientId);
	
	void handle(Transport transport, Request request, Response response) throws IOException, ServiceException;
	
	Server getServer();
	
	void setServer(Server server);

}
