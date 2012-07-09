package evymind.vapor.service;

import evymind.vapor.server.ServiceContext;

public class ContextDestroyedEvent extends AbstractServiceContextEvent {

	public ContextDestroyedEvent(ServiceContext context) {
		super(context);
	}

}
