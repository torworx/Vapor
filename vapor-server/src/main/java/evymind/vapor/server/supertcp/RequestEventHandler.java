package evymind.vapor.server.supertcp;

import evymind.vapor.core.event.handling.annontation.EventHandler;
import evymind.vapor.core.supertcp.PackageAck;
import evymind.vapor.server.Request;
import evymind.vapor.server.Response;

class RequestEventHandler {
	
	private final BaseSuperTCPConnector connector;
	
	public RequestEventHandler(BaseSuperTCPConnector connector) {
		this.connector = connector;
	}

	/**
	 * Run in thread
	 * @param event
	 */
	@EventHandler
	protected void doHandleRequest(RequestEvent event) {
		Request request = event.getRequest();
		Response response = event.getResponse();
		SCServerWorker worker = request.getTransport();
		
		worker.startRequest(request);
		connector.dispatchMessage(worker, request, response);
		if (response.getData().readableBytes() > connector.getMaxPackageSize()) {
			worker.sendError(request.getRequestId(), PackageAck.NOACK_MESSAGE_TOO_LARGE);
		} else {
			worker.sendPackage(request.getRequestId(), response.getData());
		}
		worker.completeRequest(request); // !!!IMPORTANT, return the request to request pool
		worker.completeResponse(response); // !!!IMPORTANT, return the response to response pool
	}
}
