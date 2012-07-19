package evymind.vapor.core.utils.component;

import java.util.EventListener;

public interface Lifecycle {


	/**
	 * Starts the component.
	 * 
	 * @throws Exception
	 *             If the component fails to start
	 * @see #isStarted()
	 * @see #stop()
	 * @see #isFailed()
	 */
	public void start() throws Exception;


	/**
	 * Stops the component. The component may wait for current activities to
	 * complete normally, but it can be interrupted.
	 * 
	 * @exception Exception
	 *                If the component fails to stop
	 * @see #isStopped()
	 * @see #start()
	 * @see #isFailed()
	 */
	public void stop() throws Exception;


	/**
	 * @return true if the component is starting or has been started.
	 */
	public boolean isRunning();


	/**
	 * @return true if the component has been started.
	 * @see #start()
	 * @see #isStarting()
	 */
	public boolean isStarted();


	/**
	 * @return true if the component is starting.
	 * @see #isStarted()
	 */
	public boolean isStarting();


	/**
	 * @return true if the component is stopping.
	 * @see #isStopped()
	 */
	public boolean isStopping();


	/**
	 * @return true if the component has been stopped.
	 * @see #stop()
	 * @see #isStopping()
	 */
	public boolean isStopped();


	/**
	 * @return true if the component has failed to start or has failed to stop.
	 */
	public boolean isFailed();


	public void addLifecycleListener(Lifecycle.Listener listener);


	public void removeLifecycleListener(Lifecycle.Listener listener);


	/**
	 * Listener. A listener for Lifecycle events.
	 */
	public interface Listener extends EventListener {
		
		public void lifeCycleStarting(Lifecycle event);

		public void lifeCycleStarted(Lifecycle event);

		public void lifeCycleFailure(Lifecycle event, Throwable cause);

		public void lifeCycleStopping(Lifecycle event);

		public void lifeCycleStopped(Lifecycle event);
	}
}
