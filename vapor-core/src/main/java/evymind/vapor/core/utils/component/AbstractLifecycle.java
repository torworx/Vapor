package evymind.vapor.core.utils.component;

import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AbstractLifecycle implements Lifecycle {

	private static final Logger log = LoggerFactory.getLogger(AbstractLifecycle.class);
	
	public static final String STOPPED_STRING = "STOPPED";
	public static final String FAILED_STRING = "FAILED";
	public static final String STARTING_STRING = "STARTING";
	public static final String STARTED_STRING = "STARTED";
	public static final String STOPPING_STRING = "STOPPING";
	public static final String RUNNING_STRING = "RUNNING";

	private final Object lock = new Object();
	private final int __FAILED = -1, __STOPPED = 0, __STARTING = 1, __STARTED = 2, __STOPPING = 3;
	private volatile int state = __STOPPED;

	protected final CopyOnWriteArrayList<Lifecycle.Listener> _listeners = new CopyOnWriteArrayList<Lifecycle.Listener>();

	protected void doStart() throws Exception {
	}

	protected void doStop() throws Exception {
	}

	public final void start() throws Exception {
		synchronized (lock) {
			try {
				if (state == __STARTED || state == __STARTING)
					return;
				setStarting();
				doStart();
				setStarted();
			} catch (Exception e) {
				setFailed(e);
				throw e;
			} catch (Error e) {
				setFailed(e);
				throw e;
			}
		}
	}

	public final void stop() throws Exception {
		synchronized (lock) {
			try {
				if (state == __STOPPING || state == __STOPPED)
					return;
				setStopping();
				doStop();
				setStopped();
			} catch (Exception e) {
				setFailed(e);
				throw e;
			} catch (Error e) {
				setFailed(e);
				throw e;
			}
		}
	}

	public boolean isRunning() {
		final int state = this.state;

		return state == __STARTED || state == __STARTING;
	}

	public boolean isStarted() {
		return state == __STARTED;
	}

	public boolean isStarting() {
		return state == __STARTING;
	}

	public boolean isStopping() {
		return state == __STOPPING;
	}

	public boolean isStopped() {
		return state == __STOPPED;
	}

	public boolean isFailed() {
		return state == __FAILED;
	}

	public void addLifecycleListener(Lifecycle.Listener listener) {
		_listeners.add(listener);
	}

	public void removeLifecycleListener(Lifecycle.Listener listener) {
		_listeners.remove(listener);
	}
	
	protected void checkNotStarted() {
        if (isStarted()) {
            throw new IllegalStateException(STARTED_STRING);
        }
	}

	public String getState() {
		switch (state) {
		case __FAILED:
			return FAILED_STRING;
		case __STARTING:
			return STARTING_STRING;
		case __STARTED:
			return STARTED_STRING;
		case __STOPPING:
			return STOPPING_STRING;
		case __STOPPED:
			return STOPPED_STRING;
		}
		return null;
	}

	public static String getState(Lifecycle lc) {
		if (lc.isStarting())
			return STARTING_STRING;
		if (lc.isStarted())
			return STARTED_STRING;
		if (lc.isStopping())
			return STOPPING_STRING;
		if (lc.isStopped())
			return STOPPED_STRING;
		return FAILED_STRING;
	}

	private void setStarted() {
		state = __STARTED;
		log.debug("{} {}", STARTED_STRING, this);
		for (Listener listener : _listeners)
			listener.lifecycleStarted(this);
	}

	private void setStarting() {
		log.debug("starting {}", this);
		state = __STARTING;
		for (Listener listener : _listeners)
			listener.lifecycleStarting(this);
	}

	private void setStopping() {
		log.debug("stopping {}", this);
		state = __STOPPING;
		for (Listener listener : _listeners)
			listener.lifecycleStopping(this);
	}

	private void setStopped() {
		state = __STOPPED;
		log.debug("{} {}", STOPPED_STRING, this);
		for (Listener listener : _listeners)
			listener.lifecycleStopped(this);
	}

	private void setFailed(Throwable th) {
		state = __FAILED;
		log.warn(FAILED_STRING + " " + this + ": " + th, th);
		for (Listener listener : _listeners)
			listener.lifecycleFailure(this, th);
	}

	public static abstract class AbstractLifecycleListener implements Lifecycle.Listener {
		
		public void lifecycleFailure(Lifecycle event, Throwable cause) {
		}

		public void lifecycleStarted(Lifecycle event) {
		}

		public void lifecycleStarting(Lifecycle event) {
		}

		public void lifecycleStopped(Lifecycle event) {
		}

		public void lifecycleStopping(Lifecycle event) {
		}
	}

}
