package evymind.vapor.service;

import evymind.vapor.server.ServiceContext;

public class ContextInitializedEvent extends AbstractServiceContextEvent {

	public ContextInitializedEvent(ServiceContext context) {
		super(context);
	}

}
