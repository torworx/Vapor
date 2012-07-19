package evymind.vapor.server.handler;

import java.io.IOException;

import evymind.vapor.core.Transport;
import evymind.vapor.server.Request;
import evymind.vapor.server.Response;
import evymind.vapor.server.ServiceException;


/**
 * ScopedHandler.
 * 
 * A ScopedHandler is a HandlerWrapper where the wrapped handlers each define a scope. When
 * {@link #handle(Transport, Request, Response)} is called on the first ScopedHandler in a
 * chain of HandlerWrappers, the {@link #doScope(Transport, Request, Response)} method is
 * called on all contained ScopedHandlers, before the
 * {@link #doHandle(Transport, Request, Response)} method is called on all contained
 * handlers.
 * 
 * <p>
 * For example if Scoped handlers A, B & C were chained together, then the calling order would be:
 * 
 * <pre>
 * A.handle(...)
 *   A.doScope(...)
 *     B.doScope(...)
 *       C.doScope(...)
 *         A.doHandle(...)
 *           B.doHandle(...)
 * 				C.doHandle(...)
 * <pre>
 * 
 * <p>If non scoped handler X was in the chained A, B, X & C, then 
 * the calling order would be:<pre>
 * A.handle(...)
 *   A.doScope(...)
 *     B.doScope(...)
 *       C.doScope(...)
 *         A.doHandle(...)
 *           B.doHandle(...)
 *             X.handle(...)
 *               C.handle(...)
 *                 C.doHandle(...)   
 * <pre>
 * 
 * <p>A typical usage pattern is:<pre>
 *     private static class MyHandler extends ScopedHandler
 *     {
 *         public void doScope(String target, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 *         {
 *             try
 *             {
 *                 setUpMyScope();
 *                 super.doScope(target,request,response);
 *             }
 *             finally
 *             {
 *                 tearDownMyScope();
 *             }
 *         }
 *         
 *         public void doHandle(String target, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 *         {
 *             try
 *             {
 *                 doMyHandling();
 *                 super.doHandle(target,request,response);
 *             }
 *             finally
 *             {
 *                 cleanupMyHandling();
 *             }
 *         }
 *     }
 * </pre>
 */
public abstract class ScopedHandler extends HandlerWrapper {
	
	private static final ThreadLocal<ScopedHandler> currentOuterScope = new ThreadLocal<ScopedHandler>();
	protected ScopedHandler outerScope;
	protected ScopedHandler nextScope;


	/**
	 * @see HandlerWrapper#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		try {
			outerScope = currentOuterScope.get();
			if (outerScope == null) {
				currentOuterScope.set(this);
			}
			
			super.doStart();

			nextScope = (ScopedHandler) getChildHandlerByClass(ScopedHandler.class);

		} finally {
			if (outerScope == null) {
				currentOuterScope.set(null);
			}
		}
	}


	/* 
     */
	@Override
	public final void handle(Transport transport, Request request, Response response) throws IOException, ServiceException {
		if (outerScope == null)
			doScope(transport, request, response);
		else
			doHandle(transport, request, response);
	}


	/*
	 * Scope the handler
	 */
	public abstract void doScope(Transport transport, Request request, Response response) throws IOException, ServiceException;


	/*
	 * Scope the handler
	 */
	public final void nextScope(Transport transport, Request request, Response response) throws IOException, ServiceException {
		// this method has been manually inlined in several locations, but
		// is called protected by an if(never()), so your IDE can find those
		// locations if this code is changed.
		if (nextScope != null)
			nextScope.doScope(transport, request, response);
		else if (outerScope != null)
			outerScope.doHandle(transport, request, response);
		else
			doHandle(transport, request, response);
	}


	/*
	 * Do the handler work within the scope.
	 */
	public abstract void doHandle(Transport transport, Request request, Response response) throws IOException, ServiceException ;


	/*
	 * Do the handler work within the scope.
	 */
	public final void nextHandle(Transport transport, Request request, Response response) throws IOException, ServiceException {
		// this method has been manually inlined in several locations, but
		// is called protected by an if(never()), so your IDE can find those
		// locations if this code is changed.
		if (nextScope != null && nextScope == this.handler)
			nextScope.doHandle(transport, request, response);
		else if (this.handler != null)
			this.handler.handle(transport, request, response);
	}


	protected boolean never() {
		return false;
	}

}
