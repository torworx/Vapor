package evymind.vapor.server.handler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.core.Transport;
import evymind.vapor.server.Handler;
import evymind.vapor.server.Request;
import evymind.vapor.server.Response;
import evymind.vapor.server.ServiceException;


/**
 * ContextHandlerCollection.
 * 
 * The contexts do not need to be directly contained, only
 * children of the contained handlers. Multiple contexts may have the same context path and they are called in order
 * until one handles the request.
 *
 */
public class ContextHandlerCollection extends HandlerCollection {

	private static final Logger LOG = LoggerFactory.getLogger(ContextHandlerCollection.class);

	// private volatile PathMap _contextMap;
	private Class<? extends ContextHandler> contextClass = ContextHandler.class;


	public ContextHandlerCollection() {
		super(true);
	}


	/**
	 * Remap the context paths.
	 */
	public void mapContexts() {
		// PathMap contextMap = new PathMap();
		// Handler[] branches = getHandlers();
		//
		// for (int b = 0; branches != null && b < branches.length; b++) {
		// Handler[] handlers = null;
		//
		// if (branches[b] instanceof ContextHandler) {
		// handlers = new Handler[] { branches[b] };
		// } else if (branches[b] instanceof HandlerContainer) {
		// handlers = ((HandlerContainer) branches[b]).getChildHandlersByClass(ContextHandler.class);
		// } else
		// continue;
		//
		// for (int i = 0; i < handlers.length; i++) {
		// ContextHandler handler = (ContextHandler) handlers[i];
		//
		// String contextPath = handler.getContextPath();
		//
		// if (contextPath == null || contextPath.indexOf(',') >= 0 || contextPath.startsWith("*"))
		// throw new IllegalArgumentException("Illegal context spec:" + contextPath);
		//
		// if (!contextPath.startsWith("/"))
		// contextPath = '/' + contextPath;
		//
		// if (contextPath.length() > 1) {
		// if (contextPath.endsWith("/"))
		// contextPath += "*";
		// else if (!contextPath.endsWith("/*"))
		// contextPath += "/*";
		// }
		//
		// Object contexts = contextMap.get(contextPath);
		// String[] vhosts = handler.getVirtualHosts();
		//
		// if (vhosts != null && vhosts.length > 0) {
		// Map hosts;
		//
		// if (contexts instanceof Map)
		// hosts = (Map) contexts;
		// else {
		// hosts = new HashMap();
		// hosts.put("*", contexts);
		// contextMap.put(contextPath, hosts);
		// }
		//
		// for (int j = 0; j < vhosts.length; j++) {
		// String vhost = vhosts[j];
		// contexts = hosts.get(vhost);
		// contexts = LazyList.add(contexts, branches[b]);
		// hosts.put(vhost, contexts);
		// }
		// } else if (contexts instanceof Map) {
		// Map hosts = (Map) contexts;
		// contexts = hosts.get("*");
		// contexts = LazyList.add(contexts, branches[b]);
		// hosts.put("*", contexts);
		// } else {
		// contexts = LazyList.add(contexts, branches[b]);
		// contextMap.put(contextPath, contexts);
		// }
		// }
		// }
		// _contextMap = contextMap;

	}


	/*
	 * @see evymind.vapor.server.handler.HandlerCollection#setHandlers(evymind.vapor.server.Handler[])
	 */
	@Override
	public void setHandlers(Handler[] handlers) {
		// _contextMap = null;
		super.setHandlers(handlers);
		if (isStarted())
			mapContexts();
	}


	@Override
	protected void doStart() throws Exception {
		mapContexts();
		super.doStart();
	}

	@Override
	public void handle(Transport transport, Request request, Response response) throws IOException, ServiceException {
		Handler[] handlers = getHandlers();
		if (handlers == null || handlers.length == 0)
			return;

		super.handle(transport, request, response);
		
//		AsyncContinuation async = baseRequest.getAsyncContinuation();
//		if (async.isAsync()) {
//			ContextHandler context = async.getContextHandler();
//			if (context != null) {
//				context.handle(target, baseRequest, request, response);
//				return;
//			}
//		}

		// data structure which maps a request to a context; first-best match wins
		// { context path =>
		// { virtual host => context }
		// }
//		PathMap map = _contextMap;
//		if (map != null && target != null && target.startsWith("/")) {
//			// first, get all contexts matched by context path
//			Object contexts = map.getLazyMatches(target);
//
//			for (int i = 0; i < LazyList.size(contexts); i++) {
//				// then, match against the virtualhost of each context
//				Map.Entry entry = (Map.Entry) LazyList.get(contexts, i);
//				Object list = entry.getValue();
//
//				if (list instanceof Map) {
//					Map hosts = (Map) list;
//					String host = normalizeHostname(request.getServerName());
//
//					// explicitly-defined virtual hosts, most specific
//					list = hosts.get(host);
//					for (int j = 0; j < LazyList.size(list); j++) {
//						Handler handler = (Handler) LazyList.get(list, j);
//						handler.handle(target, baseRequest, request, response);
//						if (baseRequest.isHandled())
//							return;
//					}
//
//					// wildcard for one level of names
//					list = hosts.get("*." + host.substring(host.indexOf(".") + 1));
//					for (int j = 0; j < LazyList.size(list); j++) {
//						Handler handler = (Handler) LazyList.get(list, j);
//						handler.handle(target, baseRequest, request, response);
//						if (baseRequest.isHandled())
//							return;
//					}
//
//					// no virtualhosts defined for the context, least specific
//					// will handle any request that does not match to a specific virtual host above
//					list = hosts.get("*");
//					for (int j = 0; j < LazyList.size(list); j++) {
//						Handler handler = (Handler) LazyList.get(list, j);
//						handler.handle(target, baseRequest, request, response);
//						if (baseRequest.isHandled())
//							return;
//					}
//				} else {
//					for (int j = 0; j < LazyList.size(list); j++) {
//						Handler handler = (Handler) LazyList.get(list, j);
//						handler.handle(target, baseRequest, request, response);
//						if (baseRequest.isHandled())
//							return;
//					}
//				}
//			}
//		} else {
//			// This may not work in all circumstances... but then I think it should never be called
//			for (int i = 0; i < handlers.length; i++) {
//				handlers[i].handle(target, baseRequest, request, response);
//				if (baseRequest.isHandled())
//					return;
//			}
//		}
	}


	/**
	 * Add a context handler.
	 * 
	 * @param contextPath
	 *            The context path to add
	 * @return the ContextHandler just added
	 */
	public ContextHandler addContext(String contextPath, String resourceBase) {
		try {
			ContextHandler context = contextClass.newInstance();
			context.setContextPath(contextPath);
//			context.setResourceBase(resourceBase);
			addHandler(context);
			return context;
		} catch (Exception e) {
			LOG.debug(e.getMessage());
			throw new Error(e);
		}
	}


	/**
	 * @return The class to use to add new Contexts
	 */
	public Class<? extends ContextHandler> getContextClass() {
		return contextClass;
	}


	/**
	 * @param contextClass
	 *            The class to use to add new Contexts
	 */
	public void setContextClass(Class<? extends ContextHandler> contextClass) {
		if (contextClass == null || !(ContextHandler.class.isAssignableFrom(contextClass)))
			throw new IllegalArgumentException();
		this.contextClass = contextClass;
	}


//	private String normalizeHostname(String host) {
//		if (host == null)
//			return null;
//
//		if (host.endsWith("."))
//			return host.substring(0, host.length() - 1);
//
//		return host;
//	}

}