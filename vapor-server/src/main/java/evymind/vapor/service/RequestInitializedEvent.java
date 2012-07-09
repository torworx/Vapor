package evymind.vapor.service;

import evymind.vapor.server.ServiceContext;
import evymind.vapor.server.ServiceRequest;

public class RequestInitializedEvent extends AbstractServiceRequestEvent {

	public RequestInitializedEvent(ServiceContext context, ServiceRequest request) {
		super(context, request);
	}

}
