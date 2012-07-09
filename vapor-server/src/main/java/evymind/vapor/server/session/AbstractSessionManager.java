package evymind.vapor.server.session;

import static java.lang.Math.round;

import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import evymind.vapor.core.utils.component.AbstractLifecycle;
import evymind.vapor.core.utils.statistic.CounterStatistic;
import evymind.vapor.core.utils.statistic.SampleStatistic;
import evymind.vapor.server.Session;
import evymind.vapor.server.SessionAttributeListener;
import evymind.vapor.server.SessionBindingEvent;
import evymind.vapor.server.SessionEvent;
import evymind.vapor.server.SessionListener;
import evymind.vapor.server.SessionManager;

public abstract class AbstractSessionManager extends AbstractLifecycle implements SessionManager {

	// Setting of max inactive interval for new sessions
	// -1 means no timeout
	protected int dftMaxIdleSecs = -1;

	protected final List<SessionListener> sessionListeners = new CopyOnWriteArrayList<SessionListener>();
	protected final List<SessionAttributeListener> sessionAttributeListeners = new CopyOnWriteArrayList<SessionAttributeListener>();

	protected ClassLoader loader;

	protected final CounterStatistic sessionsStats = new CounterStatistic();
	protected final SampleStatistic sessionTimeStats = new SampleStatistic();

	/* ----------------------------------------------------------- */
	public AbstractSessionManager() {
		super();
	}

	@Override
	protected void doStart() throws Exception {
		loader = Thread.currentThread().getContextClassLoader();
		super.doStart();
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();
		invalidateSessions();
		loader = null;
	}

	public abstract Session getSession(String id);

	protected abstract void invalidateSessions() throws Exception;

	@Override
	public Session createSession(String id) {
		AbstractSession session = doCreateSession(id);
		session.setMaxInactiveInterval(dftMaxIdleSecs);
		addSession(session, true);
		return session;
	}

	protected abstract AbstractSession doCreateSession(String id);

	protected void addSession(AbstractSession session, boolean created) {
		// TODO: check synchronized (this.sessionIdManager)
		synchronized (this) {
			addSession(session);
		}

		if (created) {
			this.sessionsStats.increment();
			if (sessionListeners != null) {
				SessionEvent event = new SessionEvent(session);
				for (SessionListener listener : this.sessionListeners)
					listener.sessionCreated(event);
			}
		}
	}

	protected abstract void addSession(AbstractSession session);

	@Override
	public int getMaxInactiveInterval() {
		return dftMaxIdleSecs;
	}

	@Override
	public void setMaxInactiveInterval(int seconds) {
		this.dftMaxIdleSecs = seconds;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return maximum number of sessions
	 */
	public int getSessionsMax() {
		return (int) this.sessionsStats.getMax();
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return total number of sessions
	 */
	public int getSessionsTotal() {
		return (int) this.sessionsStats.getTotal();
	}

	/* ------------------------------------------------------------ */
	public int getSessions() {
		return (int) this.sessionsStats.getCurrent();
	}

	/* ------------------------------------------------------------ */
	public boolean isValid(Session session) {
		AbstractSession s = ((SessionIf) session).getSession();
		return s.isValid();
	}

	@Override
	public void addEventListener(EventListener listener) {
		if (listener instanceof SessionListener) {
			sessionListeners.add((SessionListener) listener);
		}
		if (listener instanceof SessionAttributeListener) {
			sessionAttributeListeners.add((SessionAttributeListener) listener);
		}
	}

	@Override
	public void removeEventListener(EventListener listener) {
		if (listener instanceof SessionListener) {
			sessionListeners.remove(listener);
		}
		if (listener instanceof SessionAttributeListener) {
			sessionAttributeListeners.remove(listener);
		}
	}

	@Override
	public void clearEventListeners() {
		sessionListeners.clear();
		sessionAttributeListeners.clear();
	}

	/* ------------------------------------------------------------ */
	public void complete(Session session) {
		AbstractSession s = ((SessionIf) session).getSession();
		s.complete();
	}

	/* ------------------------------------------------------------ */
	/**
	 * Reset statistics values
	 */
	public void statsReset() {
		this.sessionsStats.reset(getSessions());
		this.sessionTimeStats.reset();
	}

	/* ------------------------------------------------------------ */
	/**
	 * Remove session from manager
	 * 
	 * @param session
	 *            The session to remove
	 * @param invalidate
	 *            True if {@link SessionListener#sessionDestroyed(SessionEvent)}
	 *            and {@link SessionIdManager#invalidateAll(String)} should be
	 *            called.
	 */
	public void removeSession(Session session, boolean invalidate) {
		AbstractSession s = ((SessionIf) session).getSession();
		removeSession(s, invalidate);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Remove session from manager
	 * 
	 * @param session
	 *            The session to remove
	 * @param invalidate
	 *            True if {@link SessionListener#sessionDestroyed(SessionEvent)}
	 *            and {@link SessionIdManager#invalidateAll(String)} should be
	 *            called.
	 */
	public void removeSession(AbstractSession session, boolean invalidate) {
		// Remove session from context and global maps
		boolean removed = removeSession(session.getId());

		if (removed) {
			this.sessionsStats.decrement();
			this.sessionTimeStats.set(round((System.currentTimeMillis() - session.getCreationTime()) / 1000.0));

			// Remove session from all context and global id maps
			// this.sessionIdManager.removeSession(session);
			// if (invalidate)
			// this.sessionIdManager.invalidateAll(session.getClusterId());

			if (invalidate && sessionListeners != null) {
				SessionEvent event = new SessionEvent(session);
				for (SessionListener listener : sessionListeners)
					listener.sessionDestroyed(event);
			}
		}
	}

	/* ------------------------------------------------------------ */
	protected abstract boolean removeSession(String id);

	/* ------------------------------------------------------------ */
	/**
	 * @return maximum amount of time session remained valid
	 */
	public long getSessionTimeMax() {
		return this.sessionTimeStats.getMax();
	}

	/**
	 * Interface that any session wrapper should implement so that
	 * SessionManager may access the Jetty session implementation.
	 * 
	 */
	public interface SessionIf extends Session {
		public AbstractSession getSession();
	}

	public void doSessionAttributeListeners(AbstractSession session, String name, Object old, Object value) {
		if (!sessionAttributeListeners.isEmpty()) {
			SessionBindingEvent event = new SessionBindingEvent(session, name, old == null ? value : old);

			for (SessionAttributeListener l : sessionAttributeListeners) {
				if (old == null)
					l.attributeAdded(event);
				else if (value == null)
					l.attributeRemoved(event);
				else
					l.attributeReplaced(event);
			}
		}
	}

}
