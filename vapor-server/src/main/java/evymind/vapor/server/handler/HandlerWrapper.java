package evymind.vapor.server.handler;

import java.io.IOException;

import evymind.vapor.core.Transport;
import evymind.vapor.server.Handler;
import evymind.vapor.server.Request;
import evymind.vapor.server.Response;
import evymind.vapor.server.Server;
import evymind.vapor.server.ServiceException;

/* ------------------------------------------------------------ */
/**
 * A <code>HandlerWrapper</code> acts as a {@link Handler} but delegates the
 * {@link Handler#handle handle} method and {@link Lifecycle life cycle} events
 * to a delegate. This is primarily used to implement the <i>Decorator</i>
 * pattern.
 * 
 */
public class HandlerWrapper extends AbstractHandlerContainer {
	
	protected Handler handler;

	/* ------------------------------------------------------------ */
	/**
     *
     */
	public HandlerWrapper() {
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return Returns the handlers.
	 */
	public Handler getHandler() {
		return this.handler;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return Returns the handlers.
	 */
	public Handler[] getHandlers() {
		if (this.handler == null)
			return new Handler[0];
		return new Handler[] { this.handler };
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param handler
	 *            Set the {@link Handler} which should be wrapped.
	 */
	public void setHandler(Handler handler) {
		if (isStarted())
			throw new IllegalStateException(STARTED_STRING);

		Handler old_handler = this.handler;
		this.handler = handler;
		if (handler != null)
			handler.setServer(getServer());

		if (getServer() != null)
			getServer().getContainer().update(this, old_handler, handler, "handler");
	}

	/* ------------------------------------------------------------ */
	/*
	 * @see org.eclipse.thread.AbstractLifecycle#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		if (this.handler != null)
			this.handler.start();
		super.doStart();
	}

	/* ------------------------------------------------------------ */
	/*
	 * @see org.eclipse.thread.AbstractLifecycle#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		if (this.handler != null)
			this.handler.stop();
		super.doStop();
	}

	/* ------------------------------------------------------------ */
	public void handle(Transport transport, Request request, Response response) throws IOException, ServiceException  {
		if (this.handler != null && isStarted()) {
			this.handler.handle(transport, request, response);
		}
	}

	/* ------------------------------------------------------------ */
	@Override
	public void setServer(Server server) {
		Server old_server = getServer();
		if (server == old_server)
			return;

		if (isStarted())
			throw new IllegalStateException(STARTED_STRING);

		super.setServer(server);

		Handler h = getHandler();
		if (h != null)
			h.setServer(server);

		if (server != null && server != old_server)
			server.getContainer().update(this, null, this.handler, "handler");
	}

	/* ------------------------------------------------------------ */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object expandChildren(Object list, Class byClass) {
		return expandHandler(this.handler, list, byClass);
	}

	/* ------------------------------------------------------------ */
	@SuppressWarnings("unchecked")
	public <H extends Handler> H getNestedHandlerByClass(Class<H> byclass) {
		HandlerWrapper h = this;
		while (h != null) {
			if (byclass.isInstance(h))
				return (H) h;
			Handler w = h.getHandler();
			if (w instanceof HandlerWrapper)
				h = (HandlerWrapper) w;
			else
				break;
		}
		return null;

	}

	/* ------------------------------------------------------------ */
	@Override
	public void destroy() {
		if (!isStopped())
			throw new IllegalStateException("!STOPPED");
		Handler child = getHandler();
		if (child != null) {
			setHandler(null);
			child.destroy();
		}
		super.destroy();
	}

}