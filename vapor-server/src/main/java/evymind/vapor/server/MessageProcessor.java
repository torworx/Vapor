package evymind.vapor.server;

import evymind.vapor.core.Transport;

public interface MessageProcessor {

	boolean processMessage(Transport transport, Request request, Response response);
	
}
