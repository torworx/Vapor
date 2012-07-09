package evymind.vapor.service;

import evymind.vapor.server.ServiceContext;
import evymind.vapor.server.ServiceRequest;

public class AbstractServiceRequestEvent {
	
	private final ServiceContext context;
	
	private final ServiceRequest request;

	public AbstractServiceRequestEvent(ServiceContext context, ServiceRequest request) {
		this.context = context;
		this.request = request;
	}

	public ServiceContext getContext() {
		return context;
	}

	public ServiceRequest getRequest() {
		return request;
	}

}
