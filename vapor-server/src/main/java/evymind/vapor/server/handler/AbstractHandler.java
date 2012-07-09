package evymind.vapor.server.handler;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.core.Transport;
import evymind.vapor.core.utils.component.AggregateLifecycle;
import evymind.vapor.server.Handler;
import evymind.vapor.server.Server;

public abstract class AbstractHandler extends AggregateLifecycle implements Handler {

	private static final Logger log = LoggerFactory.getLogger(AbstractHandler.class);
	
	private Server server;

	/* ------------------------------------------------------------ */
	/*
	 * @see org.eclipse.thread.Lifecycle#start()
	 */
	@Override
	protected void doStart() throws Exception {
		log.debug("starting {}", this);
		super.doStart();
	}

	/* ------------------------------------------------------------ */
	/*
	 * @see org.eclipse.thread.Lifecycle#stop()
	 */
	@Override
	protected void doStop() throws Exception {
		log.debug("stopping {}", this);
		super.doStop();
	}

	@Override
	public void connected(Transport transport, UUID clientId) {
		log.debug("Client [{}] connected", clientId);
	}

	@Override
	public void disconnected(Transport transport, UUID clientId) {
		log.debug("Client [{}] disconnected", clientId);
	}

	/* ------------------------------------------------------------ */
	public void setServer(Server server) {
		Server old_server = this.server;
		if (old_server != null && old_server != server)
			old_server.getContainer().removeBean(this);
		this.server = server;
		if (this.server != null && this.server != old_server)
			this.server.getContainer().addBean(this);
	}

	/* ------------------------------------------------------------ */
	public Server getServer() {
		return this.server;
	}

	/* ------------------------------------------------------------ */
	public void destroy() {
		if (!isStopped())
			throw new IllegalStateException("!STOPPED");
		super.destroy();
		if (this.server != null)
			this.server.getContainer().removeBean(this);
	}

	/* ------------------------------------------------------------ */
	public void dumpThis(Appendable out) throws IOException {
		out.append(toString()).append(" - ").append(getState()).append('\n');
	}

}
