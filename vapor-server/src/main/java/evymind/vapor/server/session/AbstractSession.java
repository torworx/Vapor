package evymind.vapor.server.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.server.SessionActivationListener;
import evymind.vapor.server.SessionBindingEvent;
import evymind.vapor.server.SessionBindingListener;
import evymind.vapor.server.SessionEvent;

public class AbstractSession implements AbstractSessionManager.SessionIf {

	private static final Logger log = LoggerFactory.getLogger(AbstractSession.class);

	private final AbstractSessionManager manager;
	private final String id;
	private final Map<String, Object> attributes = new HashMap<String, Object>();
	private boolean idChanged;
	private final long created;
	private long cookieSet;
	private long accessed; // the time of the last access
	private long lastAccessed; // the time of the last access excluding this
								// one
	private boolean invalid;
	private boolean doInvalidate;
	private long maxIdleMs;
	private boolean newSession;
	private int requests;

	/* ------------------------------------------------------------- */
	protected AbstractSession(AbstractSessionManager abstractSessionManager, String id) {
		this.manager = abstractSessionManager;

		this.newSession = true;
		this.created = System.currentTimeMillis();
		this.id = id;
		this.accessed = this.created;
		this.lastAccessed = this.created;
		this.requests = 1;
		this.maxIdleMs = this.manager.dftMaxIdleSecs > 0 ? this.manager.dftMaxIdleSecs * 1000L : -1;
		log.debug("new session & id %s", this.id);
	}

	/* ------------------------------------------------------------- */
	protected AbstractSession(AbstractSessionManager abstractSessionManager, long created, long accessed, String id) {
		this.manager = abstractSessionManager;
		this.created = created;
		this.id = id;
		this.accessed = accessed;
		this.lastAccessed = accessed;
		this.requests = 1;
		log.debug("new session " + this.id);
	}

	/* ------------------------------------------------------------- */
	/**
	 * asserts that the session is valid
	 */
	protected void checkValid() throws IllegalStateException {
		if (this.invalid)
			throw new IllegalStateException();
	}

	/* ------------------------------------------------------------- */
	public AbstractSession getSession() {
		return this;
	}

	/* ------------------------------------------------------------- */
	public long getAccessed() {
		synchronized (this) {
			return this.accessed;
		}
	}


	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		synchronized (this) {
			checkValid();
			return (T) this.attributes.get(name);
		}
	}


	public int getAttributes() {
		synchronized (this) {
			checkValid();
			return this.attributes.size();
		}
	}


	@SuppressWarnings({ "unchecked" })
	public Enumeration<String> getAttributeNames() {
		synchronized (this) {
			checkValid();
			List<String> names = this.attributes == null ? Collections.EMPTY_LIST : new ArrayList<String>(
					this.attributes.keySet());
			return Collections.enumeration(names);
		}
	}


	public Set<String> getNames() {
		synchronized (this) {
			return new HashSet<String>(this.attributes.keySet());
		}
	}

	/* ------------------------------------------------------------- */
	public long getCookieSetTime() {
		return this.cookieSet;
	}

	/* ------------------------------------------------------------- */
	public long getCreationTime() throws IllegalStateException {
		return this.created;
	}


	public String getId() throws IllegalStateException {
		return this.id;
	}

	/* ------------------------------------------------------------- */
	public long getLastAccessedTime() throws IllegalStateException {
		checkValid();
		return this.lastAccessed;
	}

	/* ------------------------------------------------------------- */
	public int getMaxInactiveInterval() {
		checkValid();
		return (int) (this.maxIdleMs / 1000);
	}


	protected boolean access(long time) {
		synchronized (this) {
			if (this.invalid)
				return false;
			this.newSession = false;
			this.lastAccessed = this.accessed;
			this.accessed = time;

			if (this.maxIdleMs > 0 && this.lastAccessed > 0 && this.lastAccessed + this.maxIdleMs < time) {
				invalidate();
				return false;
			}
			this.requests++;
			return true;
		}
	}


	protected void complete() {
		synchronized (this) {
			this.requests--;
			if (this.doInvalidate && this.requests <= 0)
				doInvalidate();
		}
	}

	/* ------------------------------------------------------------- */
	protected void timeout() throws IllegalStateException {
		// remove session from context and invalidate other sessions with same ID.
		this.manager.removeSession(this, true);

		// Notify listeners and unbind values
		synchronized (this) {
			if (!this.invalid) {
				if (this.requests <= 0)
					doInvalidate();
				else
					this.doInvalidate = true;
			}
		}
	}

	/* ------------------------------------------------------------- */
	public void invalidate() throws IllegalStateException {
		// remove session from context and invalidate other sessions with same ID.
		this.manager.removeSession(this, true);
		doInvalidate();
	}

	/* ------------------------------------------------------------- */
	protected void doInvalidate() throws IllegalStateException {
		try {
			log.debug("invalidate " + this.id);
			if (isValid())
				clearAttributes();
		} finally {
			synchronized (this) {
				// mark as invalid
				this.invalid = true;
			}
		}
	}

	/* ------------------------------------------------------------- */
	public void clearAttributes() {
		while (this.attributes != null && this.attributes.size() > 0) {
			ArrayList<String> keys;
			synchronized (this) {
				keys = new ArrayList<String>(this.attributes.keySet());
			}

			Iterator<String> iter = keys.iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();

				Object value;
				synchronized (this) {
					value = doPutOrRemove(key, null);
				}
				unbindValue(key, value);

				this.manager.doSessionAttributeListeners(this, key, value, null);
			}
		}
		if (this.attributes != null)
			this.attributes.clear();
	}

	/* ------------------------------------------------------------- */
	public boolean isIdChanged() {
		return this.idChanged;
	}

	/* ------------------------------------------------------------- */
	public boolean isNew() throws IllegalStateException {
		checkValid();
		return this.newSession;
	}


	public void removeAttribute(String name) {
		setAttribute(name, null);
	}


	protected Object doPutOrRemove(String name, Object value) {
		return value == null ? this.attributes.remove(name) : this.attributes.put(name, value);
	}


	protected Object doGet(String name) {
		return this.attributes.get(name);
	}


	public void setAttribute(String name, Object value) {
		Object old = null;
		synchronized (this) {
			checkValid();
			old = doPutOrRemove(name, value);
		}

		if (value == null || !value.equals(old)) {
			if (old != null)
				unbindValue(name, old);
			if (value != null)
				bindValue(name, value);

			this.manager.doSessionAttributeListeners(this, name, old, value);

		}
	}

	/* ------------------------------------------------------------- */
	public void setIdChanged(boolean changed) {
		this.idChanged = changed;
	}

	/* ------------------------------------------------------------- */
	public void setMaxInactiveInterval(int secs) {
		this.maxIdleMs = (long) secs * 1000L;
	}

	/* ------------------------------------------------------------- */
	@Override
	public String toString() {
		return this.getClass().getName() + ":" + getId() + "@" + hashCode();
	}

	/* ------------------------------------------------------------- */
	/** If value implements SessionBindingListener, call valueBound() */
	public void bindValue(java.lang.String name, Object value) {
		if (value != null && value instanceof SessionBindingListener)
			((SessionBindingListener) value).valueBound(new SessionBindingEvent(this, name));
	}


	public boolean isValid() {
		return !this.invalid;
	}

	/* ------------------------------------------------------------- */
	protected void cookieSet() {
		synchronized (this) {
			this.cookieSet = this.accessed;
		}
	}


	public int getRequests() {
		synchronized (this) {
			return this.requests;
		}
	}


	public void setRequests(int requests) {
		synchronized (this) {
			this.requests = requests;
		}
	}

	/* ------------------------------------------------------------- */
	/** If value implements SessionBindingListener, call valueUnbound() */
	public void unbindValue(java.lang.String name, Object value) {
		if (value != null && value instanceof SessionBindingListener)
			((SessionBindingListener) value).valueUnbound(new SessionBindingEvent(this, name));
	}

	/* ------------------------------------------------------------- */
	public void willPassivate() {
		synchronized (this) {
			SessionEvent event = new SessionEvent(this);
			for (Iterator<Object> iter = this.attributes.values().iterator(); iter.hasNext();) {
				Object value = iter.next();
				if (value instanceof SessionActivationListener) {
					SessionActivationListener listener = (SessionActivationListener) value;
					listener.sessionWillPassivate(event);
				}
			}
		}
	}

	/* ------------------------------------------------------------- */
	public void didActivate() {
		synchronized (this) {
			SessionEvent event = new SessionEvent(this);
			for (Iterator<Object> iter = this.attributes.values().iterator(); iter.hasNext();) {
				Object value = iter.next();
				if (value instanceof SessionActivationListener) {
					SessionActivationListener listener = (SessionActivationListener) value;
					listener.sessionDidActivate(event);
				}
			}
		}
	}

}
