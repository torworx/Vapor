package evymind.vapor.server.handler;

import java.io.IOException;

import evymind.vapor.core.Transport;
import evymind.vapor.server.Handler;
import evymind.vapor.server.Request;
import evymind.vapor.server.Response;
import evymind.vapor.server.ServiceException;


/**
 * HandlerList. This extension of {@link HandlerCollection} will call each contained handler in turn until either an
 * exception is thrown, the response is committed or a positive response status is set.
 */
public class HandlerList extends HandlerCollection {

	/**
	 * @see Handler#handle(Transport, Request, Response)
	 */
	@Override
	public void handle(Transport transport, Request request, Response response) throws IOException, ServiceException {
		Handler[] handlers = getHandlers();

		if (handlers != null && isStarted()) {
			for (int i = 0; i < handlers.length; i++) {
				handlers[i].handle(transport, request, response);
				if (request.isHandled())
					return;
			}
		}
	}
}
