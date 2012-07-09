package evymind.vapor.server.handler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.io.Resource;
import evyframework.common.io.UrlResource;
import evyframework.common.io.support.ResourcePatternResolver;
import evyframework.common.io.support.ResourcePatternUtils;
import evymind.vapor.core.Transport;
import evymind.vapor.core.event.component.EventMulticaster;
import evymind.vapor.core.event.component.SimpleEventMulticaster;
import evymind.vapor.core.utils.Attributes;
import evymind.vapor.core.utils.AttributesMap;
import evymind.vapor.core.utils.Loader;
import evymind.vapor.server.Graceful;
import evymind.vapor.server.HandlerContainer;
import evymind.vapor.server.Request;
import evymind.vapor.server.Response;
import evymind.vapor.server.Server;
import evymind.vapor.server.ServiceContext;
import evymind.vapor.server.ServiceException;
import evymind.vapor.server.event.ClientConnectedEvent;
import evymind.vapor.server.event.ClientDisconnectedEvent;
import evymind.vapor.server.invoker.ServiceScope;
import evymind.vapor.service.ContextDestroyedEvent;
import evymind.vapor.service.ContextInitializedEvent;
import evymind.vapor.service.RequestDestroyedEvent;
import evymind.vapor.service.RequestInitializedEvent;

public class ContextHandler extends ScopedHandler implements Attributes, EventMulticaster, Graceful {

	private static final Logger log = LoggerFactory.getLogger(ContextHandler.class);

	protected ResourcePatternResolver resolver = ResourcePatternUtils.getFileAsDefaultResourcePatternResolver();

	private static final ThreadLocal<Context> CURRENT_CONTEXT = new ThreadLocal<Context>();

	protected Context context;

	private final AttributesMap attributes;

	private EventMulticaster multicaster = new SimpleEventMulticaster();

	private ClassLoader classLoader;
	private String contextPath = "/";
	private String displayName;
	private Resource baseResource;

	private boolean shutdown = false;
	private boolean available = true;
	private volatile int availability; // 0=STOPPED, 1=AVAILABLE, 2=SHUTDOWN, 3=UNAVAILABLE

	private final static int STOPPED = 0, AVAILABLE = 1, SHUTDOWN = 2, UNAVAILABLE = 3;

	public ContextHandler() {
		super();
		this.context = new Context();
		attributes = new AttributesMap();
	}

	protected ContextHandler(Context context) {
		super();
		this.context = context;
		attributes = new AttributesMap();
	}

	public ContextHandler(HandlerContainer parent) {
		this();
		setParent(parent);
	}

	protected void setParent(HandlerContainer parent) {
		if (parent instanceof HandlerWrapper) {
			((HandlerWrapper) parent).setHandler(this);
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * Get the current ServletContext implementation.
	 * 
	 * @return ServletContext implementation
	 */
	public static Context getCurrentContext() {
		return CURRENT_CONTEXT.get();
	}

	public Context getServiceContext() {
		return this.context;
	}

	/* ------------------------------------------------------------ */
	/*
	 * @see evyframework.remoting.component.AbstractLifecycle#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		availability = STOPPED;

		ClassLoader oldClassLoader = null;
		Thread currentThread = null;
		Context oldContext = null;

		try {
			// Set the classloader
			if (classLoader != null) {
				currentThread = Thread.currentThread();
				oldClassLoader = currentThread.getContextClassLoader();
				currentThread.setContextClassLoader(classLoader);
			}

			oldContext = CURRENT_CONTEXT.get();
			CURRENT_CONTEXT.set(context);

			// defers the calling of super.doStart()
			startContext();

			synchronized (this) {
				availability = shutdown ? SHUTDOWN : available ? AVAILABLE : UNAVAILABLE;
			}
		} finally {
			CURRENT_CONTEXT.set(oldContext);

			// reset the classloader
			if (classLoader != null) {
				currentThread.setContextClassLoader(oldClassLoader);
			}

		}
	}

	protected void startContext() throws Exception {
		super.doStart();
		// multicast context initialized event
		multicastEvent(new ContextInitializedEvent(context));
	}

	/* ------------------------------------------------------------ */
	/*
	 * @see org.eclipse.thread.AbstractLifecycle#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		availability = STOPPED;

		ClassLoader oldClassLoader = null;
		Thread currentThread = null;

		Context oldContext = CURRENT_CONTEXT.get();
		CURRENT_CONTEXT.set(context);

		try {
			// Set the classloader
			if (classLoader != null) {
				currentThread = Thread.currentThread();
				oldClassLoader = currentThread.getContextClassLoader();
				currentThread.setContextClassLoader(classLoader);
			}

			super.doStop();

			// multicast context destroyed event
			multicastEvent(new ContextDestroyedEvent(context));

		} finally {
			log.info("stopped {}", this);
			CURRENT_CONTEXT.set(oldContext);
			// reset the classloader
			if (classLoader != null) {
				currentThread.setContextClassLoader(oldClassLoader);
			}
		}
	}

	public boolean checkContext(Transport transport, Request request, Response response) {
		switch (availability) {
		case STOPPED:
		case SHUTDOWN:
			return false;
		case UNAVAILABLE:
			// TODO send unavailable info
			// baseRequest.setHandled(true);
			// response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			return false;
		default:
			;
		}

		return true;
	}

	@Override
	public void connected(Transport transport, UUID clientId) {
		multicastEvent(new ClientConnectedEvent(transport, clientId));
	}

	@Override
	public void disconnected(Transport transport, UUID clientId) {
		multicastEvent(new ClientDisconnectedEvent(transport, clientId));
	}

	@Override
	public void doScope(Transport transport, Request request, Response response) throws IOException, ServiceException {

		Thread currentThread = null;
		ClassLoader oldClassLoader = null;

		Context oldContext = request.getContext();

		// Are we already in this context?
		if (oldContext != context) {

			// Set the classloader
			if (classLoader != null) {
				currentThread = Thread.currentThread();
				oldClassLoader = currentThread.getContextClassLoader();
				currentThread.setContextClassLoader(classLoader);
			}
		}

		try {
			// Update the paths
			request.setContext(context);
			CURRENT_CONTEXT.set(context);

			// start manual inline of nextScope(transport,request,response);
			if (never())
				nextScope(transport, request, response);
			else if (nextScope != null)
				nextScope.doScope(transport, request, response);
			else if (outerScope != null)
				outerScope.doHandle(transport, request, response);
			else
				doHandle(transport, request, response);
			// end manual inline (pathentic attempt to reduce stack depth)
		} finally {
			if (oldContext != context) {
				// reset the classloader
				if (classLoader != null) {
					currentThread.setContextClassLoader(oldClassLoader);
				}

				// reset the context
				request.setContext(oldContext);
				CURRENT_CONTEXT.set(oldContext);
			}
		}
	}

	@Override
	public void doHandle(Transport transport, Request request, Response response) throws IOException, ServiceException {

		final boolean newContext = request.takeNewContext();
		try {

			if (newContext) {
				multicastEvent(new RequestInitializedEvent(context, request));
			}

			// start manual inline of nextHandle(transport,request,response);
			// noinspection ConstantIfStatement
			if (never())
				nextHandle(transport, request, response);
			else if (nextScope != null && nextScope == handler)
				nextScope.doHandle(transport, request, response);
			else if (handler != null)
				handler.handle(transport, request, response);
			// end manual inline
		} catch (IOException e) {
			throw e;
		} catch (ServiceException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		} finally {
			// Handle more REALLY SILLY request events!
			if (newContext) {
				multicastEvent(new RequestDestroyedEvent(context, request));
			}
		}
	}
	/* ------------------------------------------------------------ */
	/**
	 * @return Returns the attributes.
	 */
	public Attributes getAttributes() {
		return attributes;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T) attributes.getAttribute(name);
	}

	/* ------------------------------------------------------------ */
	/*
	 * @see javax.servlet.ServletContext#getAttributeNames()
	 */
	public Enumeration<String> getAttributeNames() {
		return AttributesMap.getAttributeNamesCopy(attributes);
	}

	public void removeAttribute(String name) {
		attributes.removeAttribute(name);
	}

	/* ------------------------------------------------------------ */
	/*
	 * Set a context attribute. Attributes set via this API cannot be overriden by the ServletContext.setAttribute API.
	 * Their lifecycle spans the stop/start of a context. No attribute listener events are triggered by this API.
	 */
	public void setAttribute(String name, Object value) {
		attributes.setAttribute(name, value);
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param attributes
	 *            The attributes to set.
	 */
	public void setAttributes(Attributes attributes) {
		this.attributes.clearAttributes();
		this.attributes.addAll(attributes);
	}

	/* ------------------------------------------------------------ */
	public void clearAttributes() {
		attributes.clearAttributes();
	}

	@Override
	public void setServer(Server server) {
		// TODO Auto-generated method stub
		super.setServer(server);
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Resource getBaseResource() {
		return baseResource;
	}

	public void setBaseResource(Resource baseResource) {
		this.baseResource = baseResource;
	}

	// TODO Rename getResource() as getRelativeResource()
	public Resource getResource(String path) throws MalformedURLException {
		if (path == null || !path.startsWith("/")) {
			throw new MalformedURLException(path);
		}
		if (baseResource == null) {
			return null;
		}
		try {
			return baseResource.createRelative(path);
		} catch (IOException e) {
			return null;
		}
	}

	// TODO rename newResource to getResource
	/* ------------------------------------------------------------ */
	/**
	 * Convert URL to Resource wrapper for {@link Resource#newResource(URL)} enables extensions to provide alternate
	 * resource implementations.
	 */
	public Resource newResource(URL url) throws IOException {
		return new UrlResource(url);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Convert a URL or path to a Resource. The default implementation is a wrapper for
	 * {@link Resource#newResource(String)}.
	 * 
	 * @param urlOrPath
	 *            The URL or path to convert
	 * @return The Resource for the URL/path
	 * @throws IOException
	 *             The Resource could not be created.
	 */
	public Resource newResource(String urlOrPath) throws IOException {
		return resolver.getResource(urlOrPath);
	}

	/* ------------------------------------------------------------ */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();

		Package pkg = getClass().getPackage();
		if (pkg != null) {
			String p = pkg.getName();
			if (p != null && p.length() > 0) {
				String[] ss = p.split("\\.");
				for (String s : ss)
					b.append(s.charAt(0)).append('.');
			}
		}
		b.append(getClass().getSimpleName());
		b.append('{').append(getContextPath()).append(',').append(getBaseResource());
		b.append('}');

		return b.toString();
	}

	/* ------------------------------------------------------------ */
	public synchronized Class<?> loadClass(String className) throws ClassNotFoundException {
		if (className == null)
			return null;

		if (classLoader == null)
			return Loader.loadClass(this.getClass(), className);

		return classLoader.loadClass(className);
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return true if this context is accepting new requests
	 */
	public boolean isShutdown() {
		synchronized (this) {
			return !this.shutdown;
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * Set shutdown status. This field allows for graceful shutdown of a context. A started context may be put into non
	 * accepting state so that existing requests can complete, but no new requests are accepted.
	 * 
	 * @param shutdown
	 *            true if this context is (not?) accepting new requests
	 */
	@Override
	public void setShutdown(boolean shutdown) {
		synchronized (this) {
			this.shutdown = shutdown;
			this.availability = isRunning() ? (shutdown ? SHUTDOWN : this.available ? AVAILABLE : UNAVAILABLE)
					: STOPPED;
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return false if this context is unavailable (sends 503)
	 */
	public boolean isAvailable() {
		synchronized (this) {
			return this.available;
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * Set Available status.
	 */
	public void setAvailable(boolean available) {
		synchronized (this) {
			this.available = available;
			this.availability = isRunning() ? (shutdown ? SHUTDOWN : this.available ? AVAILABLE : UNAVAILABLE)
					: STOPPED;
		}
	}

	@Override
	public EventListener[] getListeners() {
		return multicaster.getListeners();
	}

	@Override
	public void addListener(EventListener listener) {
		multicaster.addListener(listener);
	}

	@Override
	public void removeListener(EventListener listener) {
		multicaster.removeListener(listener);
	}

	@Override
	public void removeAllListeners() {
		multicaster.removeAllListeners();
	}

	@Override
	public void multicastEvent(Object event) {
		multicaster.multicastEvent(event);
	}

	public void setListeners(EventListener[] listeners) {
		multicaster.setListeners(listeners);
	}

	public class Context implements ServiceContext {

		final private static String UNIMPLEMENTED = "Unimplemented - use evyframework.remoting.service.ServiceContextHandler";

		/* ------------------------------------------------------------ */
		protected Context() {
		}

		/* ------------------------------------------------------------ */
		public ContextHandler getContextHandler() {
			// TODO reduce visibility of this method
			return ContextHandler.this;
		}

		@Override
		public <T> void addService(Class<T> serviceInterface, T serviceInstance) {
			log.warn(UNIMPLEMENTED);
		}

		@Override
		public <T> void addService(Class<T> serviceInterface, Class<? extends T> serviceImplementation) {
			log.warn(UNIMPLEMENTED);
		}

		@Override
		public <T> void addService(Class<T> serviceInterface, Class<? extends T> serviceImplementation,
				ServiceScope scope) {
			log.warn(UNIMPLEMENTED);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void addListener(String className) {
			try {
				Class<? extends EventListener> clazz = classLoader == null ? Loader.loadClass(ContextHandler.class,
						className) : classLoader.loadClass(className);
				addListener(clazz);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException(e);
			} catch (LinkageError e) {
				throw new IllegalArgumentException(e);
			}

		}

		@Override
		public <T extends EventListener> void addListener(T listener) {
			ContextHandler.this.addListener(listener);
		}

		@Override
		public void addListener(Class<? extends EventListener> listenerClass) {
			try {
				EventListener listener = listenerClass.newInstance();
				addListener(listener);
			} catch (InstantiationException e) {
				throw new IllegalArgumentException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			}
		}

	}

}
