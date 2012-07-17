package evymind.vapor.server.supertcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.core.event.handling.annontation.EventHandler;
import evymind.vapor.core.supertcp.PackageAck;
import evymind.vapor.server.Request;
import evymind.vapor.server.Response;

class RequestHandler {
	
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	
	private final BaseSuperTCPConnector connector;
	
	public RequestHandler(BaseSuperTCPConnector connector) {
		this.connector = connector;
	}

	/**
	 * Run in thread
	 * @param event
	 */
	@EventHandler
	protected void doHandleRequest(RequestEvent event) {
		log.debug("Prepare process request");
		Request request = event.getRequest();
		Response response = event.getResponse();
		SCServerWorker worker = request.getTransport();
		
		worker.startRequest(request);
		log.debug("Dispatching request message to handlers");
		connector.dispatchMessage(worker, request, response);
		log.debug("Sending response to client");
		if (response.getData().readableBytes() > connector.getMaxPackageSize()) {
			worker.sendError(request.getRequestId(), PackageAck.NOACK_MESSAGE_TOO_LARGE);
		} else {
			worker.sendPackage(request.getRequestId(), response.getData());
		}
		log.debug("Completing handle request, releasing Request and Response resources");
		worker.completeRequest(request); // !!!IMPORTANT, return the request to request pool
		worker.completeResponse(response); // !!!IMPORTANT, return the response to response pool
		log.debug("Complete process request");
	}
}
