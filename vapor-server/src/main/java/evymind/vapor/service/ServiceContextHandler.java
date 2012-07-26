package evymind.vapor.service;

import java.util.EventListener;

import evymind.vapor.server.Handler;
import evymind.vapor.server.HandlerContainer;
import evymind.vapor.server.ServiceModule;
import evymind.vapor.server.handler.ContextHandler;
import evymind.vapor.server.handler.HandlerWrapper;
import evymind.vapor.server.invoker.ServiceScope;
import evymind.vapor.server.session.SessionHandler;

public class ServiceContextHandler extends ContextHandler {

	private boolean sessionsSupport;
	
	protected SessionHandler sessionHandler;
	private ServiceHandler serviceHandler;
	private HandlerWrapper wrapper;
	
	public ServiceContextHandler() {
		this(null, null, null, null);
	}
	
	public ServiceContextHandler(boolean sessionSupport) {
		this(null, null, sessionSupport);
	}
	
	public ServiceContextHandler(HandlerContainer parent, String contextPath) {
		this(parent, null, null, null);
	}
	
	public ServiceContextHandler(HandlerContainer parent, String contextPath, boolean sessionsSupport) {
		this(parent, contextPath, null, null);
		this.sessionsSupport = sessionsSupport;
	}
	
	public ServiceContextHandler(HandlerContainer parent, SessionHandler sessionHandler, ServiceHandler serviceHandler) {
		this(parent, null, sessionHandler, serviceHandler);
	}
	
	public ServiceContextHandler(HandlerContainer parent, String contextPath, SessionHandler sessionHandler, ServiceHandler serviceHandler) {
		super(null);
		this.context = new Context();
		this.sessionHandler = sessionHandler;
		this.serviceHandler = serviceHandler;
		if (contextPath != null) {
			setContextPath(contextPath);
		}
		setParent(parent);
	}

	public void configure(ServiceModule...modules) {
		for (ServiceModule module : modules) {
			module.configure(getServiceContext());
		}
	}
	
	@Override
	protected void doStop() throws Exception {
		super.doStop();
		if (wrapper != null) {
			wrapper.setHandler(null);
		}
	}

	@Override
	protected void startContext() throws Exception {
		// force creation of missing handlers.
		getSessionHandler();
		getServiceHandler();

		Handler handler = serviceHandler;
		// if (securityHandler != null) {
		// securityHandler.setHandler(handler);
		// handler = securityHandler;
		// }
		//
		if (sessionHandler != null) {
			sessionHandler.setHandler(handler);
			handler = sessionHandler;
		}

		// skip any wrapped handlers
		wrapper = this;
		while (wrapper != handler && wrapper.getHandler() instanceof HandlerWrapper) {
			wrapper = (HandlerWrapper) wrapper.getHandler();
		}

		// if we are not already linked
		if (wrapper != handler) {
			if (wrapper.getHandler() != null) {
				throw new IllegalStateException("!ScopedHandler");
			}
			wrapper.setHandler(handler);
		}

		super.startContext();

		// OK to Initialize servlet handler now
		if (serviceHandler != null && serviceHandler.isStarted()) {
//			for (int i = _decorators.size() - 1; i >= 0; i--) {
//				Decorator decorator = _decorators.get(i);
//				if (serviceHandler.getFilters() != null)
//					for (FilterHolder holder : serviceHandler.getFilters())
//						decorator.decorateFilterHolder(holder);
//				if (serviceHandler.getServlets() != null)
//					for (ServletHolder holder : serviceHandler.getServlets())
//						decorator.decorateServletHolder(holder);
//			}

			serviceHandler.initialize();
		}
	}
	
	protected SessionHandler createSessionHandler() {
		return new SessionHandler();
	}

	protected ServiceHandler createServiceHandler() {
		return new ServiceHandler();
	}
	
	public SessionHandler getSessionHandler() {
		if (sessionHandler == null && sessionsSupport && !isStarted()) {
			sessionHandler = createSessionHandler();
		}
		return sessionHandler;
	}

	public void setSessionHandler(SessionHandler _sessionHandler) {
		checkNotStarted();
		this.sessionHandler = _sessionHandler;
	}

	public ServiceHandler getServiceHandler() {
		if (serviceHandler == null && !isStarted()) {
			serviceHandler = createServiceHandler();
		}
		return serviceHandler;
	}

	public void setServiceHandler(ServiceHandler serviceHandler) {
		checkNotStarted();
		this.serviceHandler = serviceHandler;
	}

	public <T> void addService(Class<T> serviceInterface, T serviceInstance) {
		getServiceHandler().addService(serviceInterface, serviceInstance);
	}

	public <T> void addService(Class<T> serviceInterface, Class<? extends T> serviceImplementation) {
		getServiceHandler().addService(serviceInterface, serviceImplementation);
	}

	public <T> void addService(Class<T> serviceInterface, Class<? extends T> serviceImplementation, ServiceScope scope) {
		getServiceHandler().addService(serviceInterface, serviceImplementation, scope);
	}
	
	public class Context extends ContextHandler.Context {
		
		protected void checkStarting() {
			// meybe should not check this
			if (!isStarting()) {
//				throw new IllegalStateException();
			}
		}

		@Override
		public <T> void addService(Class<T> serviceInterface, T serviceInstance) {
			checkStarting();
			ServiceHandler handler = ServiceContextHandler.this.getServiceHandler();
			handler.addService(serviceInterface, serviceInstance);
		}

		@Override
		public <T> void addService(Class<T> serviceInterface, Class<? extends T> serviceImplementation) {
			checkStarting();
			ServiceHandler handler = ServiceContextHandler.this.getServiceHandler();
			handler.addService(serviceInterface, serviceImplementation);
		}

		@Override
		public <T> void addService(Class<T> serviceInterface, Class<? extends T> serviceImplementation,
				ServiceScope scope) {
			checkStarting();
			ServiceHandler handler = ServiceContextHandler.this.getServiceHandler();
			handler.addService(serviceInterface, serviceImplementation, scope);
		}

		@Override
		public void addListener(String className) {
			checkStarting();
			super.addListener(className);
		}

		@Override
		public <T extends EventListener> void addListener(T listener) {
			checkStarting();
			super.addListener(listener);
		}

		@Override
		public void addListener(Class<? extends EventListener> listenerClass) {
			checkStarting();
			super.addListener(listenerClass);
		}
		
	}

}
