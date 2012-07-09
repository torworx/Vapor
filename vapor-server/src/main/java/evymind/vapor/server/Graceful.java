package evymind.vapor.server;

/* ------------------------------------------------------------ */
/**
 * A handler that can be gracefully shutdown. Called by doStop if a
 * {@link Server#setGracefulShutdown} period is set.
 */
public interface Graceful extends Handler {
	
	public void setShutdown(boolean shutdown);
}
