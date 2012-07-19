package evymind.vapor.server;

import evymind.vapor.core.utils.component.Lifecycle;

/**
 * A Handler that contains other Handlers.
 * <p>
 * The contained handlers may be one (see @{link
 * {@link evymind.vapor.server.handler.HandlerWrapper}) or many (see
 * {@link evymind.vapor.server.handler.HandlerList} or
 * {@link evymind.vapor.server.handler.HandlerCollection}.
 * 
 */
public interface HandlerContainer extends Lifecycle {

	/**
	 * @return array of handlers directly contained by this handler.
	 */
	public Handler[] getHandlers();


	/**
	 * @return array of all handlers contained by this handler and it's children
	 */
	public Handler[] getChildHandlers();


	/**
	 * @param byclass
	 * @return array of all handlers contained by this handler and it's children
	 *         of the passed type.
	 */
	public Handler[] getChildHandlersByClass(Class<?> byclass);


	/**
	 * @param byclass
	 * @return first handler of all handlers contained by this handler and it's
	 *         children of the passed type.
	 */
	public <T extends Handler> T getChildHandlerByClass(Class<T> byclass);
}