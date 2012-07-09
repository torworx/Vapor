package evymind.vapor.service;

import evymind.vapor.server.ServiceContext;

public abstract class AbstractServiceContextEvent {

	private final ServiceContext context;

	public AbstractServiceContextEvent(ServiceContext context) {
		super();
		this.context = context;
	}

	public ServiceContext getContext() {
		return context;
	}
	
}
