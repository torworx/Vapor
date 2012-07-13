package evymind.vapor.service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.Assert;
import evymind.vapor.core.Transport;
import evymind.vapor.server.Request;
import evymind.vapor.server.Response;
import evymind.vapor.server.ServiceException;
import evymind.vapor.server.handler.ScopedHandler;
import evymind.vapor.server.invoker.DefaultServiceInvokerFactory;
import evymind.vapor.server.invoker.ServiceDefinition;
import evymind.vapor.server.invoker.ServiceInvoker;
import evymind.vapor.server.invoker.ServiceInvokerFactory;
import evymind.vapor.server.invoker.ServiceScope;

public class ServiceHandler extends ScopedHandler {
	
	private static final Logger log = LoggerFactory.getLogger(ServiceHandler.class);

	private final ServiceInvokerFactory serviceInvokerFactory = new DefaultServiceInvokerFactory();

	@Override
	protected void doStart() throws Exception {
		getServiceInvokerFactory().start();
		super.doStart();
	}

	@Override
	protected void doStop() throws Exception {
		getServiceInvokerFactory().stop();
		super.doStop();
	}

	@Override
	public void doScope(Transport transport, Request request, Response response) throws IOException, ServiceException  {
		try {
			// remember some old values in request
			// set some new values in reuqest

			// start manual inline of nextScope(target,baseRequest,request,response);
			if (never()) {
				nextScope(transport, request, response);
			} else if (nextScope != null) {
				nextScope.doScope(transport, request, response);
			} else if (outerScope != null) {
				outerScope.doHandle(transport, request, response);
			} else {
				doHandle(transport, request, response);
			}
			// end manual inline (pathetic attempt to reduce stack depth)
		} finally {
			// restore some old values in request
		}
	}

	@Override
	public void doHandle(Transport transport, Request request, Response response) throws IOException, ServiceException  {
		Assert.hasText(request.getRequestInterface(), "Invalid request interface");
		Assert.hasText(request.getRequestMethod(), "Invalid request method");

		ServiceInvoker serviceInvoker = getServiceInvokerFactory().getServiceInvoker(request.getRequestInterface());
		
		if (serviceInvoker != null) {
			try {
				log.debug("Invoking service {} -> {}", request.getRequestInterface(), request.getRequestMethod());
				serviceInvoker.invoke(request.getRequestMethod(), request, response, transport);
				log.debug("Invoked service {} -> {}", request.getRequestInterface(), request.getRequestMethod());
				response.getMessage().writeToBuffer(response.getData());
				request.markHandled();
			} catch (InvocationTargetException e) {
				throw new ServiceException(e);
			} catch (IllegalAccessException e) {
				throw new ServiceException(e);
			}
		} else {
			log.warn("Not found service [{}] in {}, skip this handler", request.getRequestInterface(), this);
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * Initialize filters and load-on-startup services. Called automatically from start if autoInitializeService is
	 * true.
	 */
	public void initialize() throws Exception {
		// TODO services init with config support ?
	}

	public ServiceInvokerFactory getServiceInvokerFactory() {
		return serviceInvokerFactory;
	}

	public <T> void addService(Class<T> serviceInterface, T serviceInstance) {
		getServiceInvokerFactory().addService(serviceInterface, serviceInstance);
	}

	public <T> void addService(Class<T> serviceInterface, Class<? extends T> serviceImplementation) {
		getServiceInvokerFactory().addService(serviceInterface, serviceImplementation);
	}

	public <T> void addService(Class<T> serviceInterface, Class<? extends T> serviceImplementation, ServiceScope scope) {
		getServiceInvokerFactory().addService(serviceInterface, serviceImplementation, scope);
	}

	public void setServices(ServiceDefinition[] serviceDefinitions) {
		getServiceInvokerFactory().setServices(serviceDefinitions);
	}

	public boolean isAvailable() {
		return isStarted() && getServiceInvokerFactory().isStarted();
	}
}
