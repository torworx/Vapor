package evymind.vapor.service;

import evymind.vapor.server.ServiceContext;
import evymind.vapor.server.ServiceRequest;

public class RequestDestroyedEvent extends AbstractServiceRequestEvent {

	public RequestDestroyedEvent(ServiceContext context, ServiceRequest request) {
		super(context, request);
	}

}
