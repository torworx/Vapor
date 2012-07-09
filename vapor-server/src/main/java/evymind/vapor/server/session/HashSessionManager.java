package evymind.vapor.server.session;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HashSessionManager extends AbstractSessionManager {

	private static final Logger log = LoggerFactory.getLogger(HashSessionManager.class);

	protected final ConcurrentMap<String, HashedSession> sessions = new ConcurrentHashMap<String, HashedSession>();
	private static int id;
	private Timer timer;
	private boolean timerStop = false;
	private TimerTask task;
	long scavengePeriodMs = 30000;
	long savePeriodMs = 0; // don't do period saves by default
	long idleSavePeriodMs = 0; // don't idle save sessions by default.
	private TimerTask saveTask;
	File storeDir;
	private boolean lazyLoad = false;
	private volatile boolean sessionsLoaded = false;
	private boolean deleteUnrestorableSessions = false;
	
	/* ------------------------------------------------------------ */
	public HashSessionManager() {
		super();
	}

	@Override
	protected void doStart() throws Exception {
		super.doStart();

		this.timerStop = false;
		if (this.timer == null) {
			this.timerStop = true;
			this.timer = new Timer("HashSessionScavenger-" + id++, true);
		}

		setScavengePeriod(getScavengePeriod());

		if (this.storeDir != null) {
			if (!this.storeDir.exists())
				this.storeDir.mkdirs();

			if (!lazyLoad)
				restoreSessions();
		}

		setSavePeriod(getSavePeriod());
	}

	@Override
	protected void doStop() throws Exception {
		// stop the scavengers
		synchronized (this) {
			if (saveTask != null)
				saveTask.cancel();
			saveTask = null;
			if (task != null)
				task.cancel();
			task = null;
			if (timer != null && timerStop)
				timer.cancel();
			timer = null;
		}

		// This will callback invalidate sessions - where we decide if we will
		// save
		super.doStop();

		sessions.clear();
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return the period in seconds at which a check is made for sessions to be
	 *         invalidated.
	 */
	public int getScavengePeriod() {
		return (int) (this.scavengePeriodMs / 1000);
	}

	/* ------------------------------------------------------------ */
	@Override
	public int getSessions() {
		int sessions = super.getSessions();
		if (this.sessions.size() != sessions)
			log.warn("sessions: " + this.sessions.size() + "!=" + sessions);
		return sessions;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return seconds Idle period after which a session is saved
	 */
	public int getIdleSavePeriod() {
		if (this.idleSavePeriodMs <= 0)
			return 0;

		return (int) (this.idleSavePeriodMs / 1000);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Configures the period in seconds after which a session is deemed idle and
	 * saved to save on session memory.
	 * 
	 * The session is persisted, the values attribute map is cleared and the
	 * session set to idled.
	 * 
	 * @param seconds
	 *            Idle period after which a session is saved
	 */
	public void setIdleSavePeriod(int seconds) {
		this.idleSavePeriodMs = seconds * 1000L;
	}

	/* ------------------------------------------------------------ */
	@Override
	public void setMaxInactiveInterval(int seconds) {
		super.setMaxInactiveInterval(seconds);
		if (this.dftMaxIdleSecs > 0 && this.scavengePeriodMs > this.dftMaxIdleSecs * 1000L)
			setScavengePeriod((this.dftMaxIdleSecs + 9) / 10);
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param seconds
	 *            the period is seconds at which sessions are periodically saved
	 *            to disk
	 */
	public void setSavePeriod(int seconds) {
		long period = (seconds * 1000L);
		if (period < 0)
			period = 0;
		this.savePeriodMs = period;

		if (this.timer != null) {
			synchronized (this) {
				if (this.saveTask != null)
					this.saveTask.cancel();

				// only save if we have a directory configured
				if (this.savePeriodMs > 0 && this.storeDir != null) {
					this.saveTask = new TimerTask() {
						@Override
						public void run() {
							try {
								saveSessions(true);
							} catch (Exception e) {
								log.warn(e.getMessage(), e);
							}
						}
					};
					this.timer.schedule(this.saveTask, this.savePeriodMs, this.savePeriodMs);
				}
			}
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return the period in seconds at which sessions are periodically saved to
	 *         disk
	 */
	public int getSavePeriod() {
		if (this.savePeriodMs <= 0)
			return 0;

		return (int) (this.savePeriodMs / 1000);
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param seconds
	 *            the period in seconds at which a check is made for sessions to
	 *            be invalidated.
	 */
	public void setScavengePeriod(int seconds) {
		if (seconds == 0)
			seconds = 60;

		long old_period = this.scavengePeriodMs;
		long period = seconds * 1000L;
		if (period > 60000)
			period = 60000;
		if (period < 1000)
			period = 1000;

		this.scavengePeriodMs = period;
		if (this.timer != null && (period != old_period || this.task == null)) {
			synchronized (this) {
				if (this.task != null)
					this.task.cancel();
				this.task = new TimerTask() {
					@Override
					public void run() {
						scavenge();
					}
				};
				this.timer.schedule(this.task, this.scavengePeriodMs, this.scavengePeriodMs);
			}
		}
	}

	/* -------------------------------------------------------------- */
	/**
	 * Find sessions that have timed out and invalidate them. This runs in the
	 * SessionScavenger thread.
	 */
	protected void scavenge() {
		// don't attempt to scavenge if we are shutting down
		if (isStopping() || isStopped())
			return;

		Thread thread = Thread.currentThread();
		ClassLoader old_loader = thread.getContextClassLoader();
		try {
			if (this.loader != null)
				thread.setContextClassLoader(this.loader);

			// For each session
			long now = System.currentTimeMillis();
			for (Iterator<HashedSession> i = this.sessions.values().iterator(); i.hasNext();) {
				HashedSession session = i.next();
				long idleTime = session.getMaxInactiveInterval() * 1000L;
				if (idleTime > 0 && session.getAccessed() + idleTime < now) {
					// Found a stale session, add it to the list
					session.timeout();
				} else if (this.idleSavePeriodMs > 0 && session.getAccessed() + this.idleSavePeriodMs < now) {
					session.idle();
				}
			}
		} catch (Throwable t) {
			log.warn("Problem scavenging sessions", t);
		} finally {
			thread.setContextClassLoader(old_loader);
		}
	}

	/* ------------------------------------------------------------ */
	@Override
	protected void addSession(AbstractSession session) {
		if (isRunning())
			this.sessions.put(session.getId(), (HashedSession) session);
	}

	/* ------------------------------------------------------------ */
	@Override
	public AbstractSession getSession(String id) {
		if (lazyLoad && !sessionsLoaded) {
			try {
				restoreSessions();
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		}

		Map<String, HashedSession> sessions = this.sessions;
		if (sessions == null)
			return null;

		HashedSession session = sessions.get(id);

		if (session == null && lazyLoad)
			session = restoreSession(id);
		if (session == null)
			return null;

		if (idleSavePeriodMs != 0)
			session.deIdle();

		return session;
	}

	/* ------------------------------------------------------------ */
	@Override
	protected void invalidateSessions() throws Exception {
		// Invalidate all sessions to cause unbind events
		ArrayList<HashedSession> sessions = new ArrayList<HashedSession>(this.sessions.values());
		int loop = 100;
		while (sessions.size() > 0 && loop-- > 0) {
			// If we are called from doStop
			if (isStopping() && storeDir != null && storeDir.exists() && storeDir.canWrite()) {
				// Then we only save and remove the session - it is not
				// invalidated.
				for (HashedSession session : sessions) {
					session.save(false);
					removeSession(session, false);
				}
			} else {
				for (HashedSession session : sessions)
					session.invalidate();
			}

			// check that no new sessions were created while we were iterating
			sessions = new ArrayList<HashedSession>(this.sessions.values());
		}
	}

	@Override
	protected AbstractSession doCreateSession(String id) {
		return new HashedSession(this, id);
	}

	/* ------------------------------------------------------------ */
	protected AbstractSession createSession(long created, long accessed, String id) {
		return new HashedSession(this, created, accessed, id);
	}

	/* ------------------------------------------------------------ */
	@Override
	protected boolean removeSession(String id) {
		return this.sessions.remove(id) != null;
	}

	/* ------------------------------------------------------------ */
	public void setStoreDirectory(File dir) {
		this.storeDir = dir;
	}

	/* ------------------------------------------------------------ */
	public File getStoreDirectory() {
		return this.storeDir;
	}

	/* ------------------------------------------------------------ */
	public void setLazyLoad(boolean lazyLoad) {
		this.lazyLoad = lazyLoad;
	}

	/* ------------------------------------------------------------ */
	public boolean isLazyLoad() {
		return this.lazyLoad;
	}

	/* ------------------------------------------------------------ */
	public boolean isDeleteUnrestorableSessions() {
		return this.deleteUnrestorableSessions;
	}

	/* ------------------------------------------------------------ */
	public void setDeleteUnrestorableSessions(boolean deleteUnrestorableSessions) {
		this.deleteUnrestorableSessions = deleteUnrestorableSessions;
	}

	/* ------------------------------------------------------------ */
	public void restoreSessions() throws Exception {
		this.sessionsLoaded = true;

		if (this.storeDir == null || !this.storeDir.exists()) {
			return;
		}

		if (!this.storeDir.canRead()) {
			log.warn("Unable to restore Sessions: Cannot read from Session storage directory "
					+ this.storeDir.getAbsolutePath());
			return;
		}

		String[] files = this.storeDir.list();
		for (int i = 0; files != null && i < files.length; i++) {
			restoreSession(files[i]);
		}
	}

	/* ------------------------------------------------------------ */
	protected synchronized HashedSession restoreSession(String id) {
		File file = new File(this.storeDir, id);
		try {
			if (file.exists()) {
				FileInputStream in = new FileInputStream(file);
				HashedSession session = restoreSession(in, null);
				in.close();
				addSession(session, false);
				session.didActivate();
				file.delete();
				return session;
			}
		} catch (Exception e) {

			if (isDeleteUnrestorableSessions()) {
				if (file.exists()) {
					file.delete();
					log.warn("Deleting file for unrestorable session " + id, e);
				}
			} else
				log.warn("Problem restoring session " + id, e);

		}
		return null;
	}

	/* ------------------------------------------------------------ */
	public void saveSessions(boolean reactivate) throws Exception {
		if (this.storeDir == null || !this.storeDir.exists()) {
			return;
		}

		if (!this.storeDir.canWrite()) {
			log.warn("Unable to save Sessions: Session persistence storage directory "
					+ this.storeDir.getAbsolutePath() + " is not writeable");
			return;
		}

		for (HashedSession session : this.sessions.values())
			session.save(true);
	}

	/* ------------------------------------------------------------ */
	public HashedSession restoreSession(InputStream is, HashedSession session) throws Exception {
		/*
		 * Take care of this class's fields first by calling defaultReadObject
		 */
		DataInputStream in = new DataInputStream(is);
		String id = in.readUTF();
		long created = in.readLong();
		long accessed = in.readLong();
		int requests = in.readInt();

		if (session == null)
			session = (HashedSession) createSession(created, accessed, id);
		session.setRequests(requests);
		int size = in.readInt();
		if (size > 0) {
			ClassLoadingObjectInputStream ois = new ClassLoadingObjectInputStream(in);
			for (int i = 0; i < size; i++) {
				String key = ois.readUTF();
				Object value = ois.readObject();
				session.setAttribute(key, value);
			}
			ois.close();
		} else
			in.close();
		return session;
	}

	/* ------------------------------------------------------------ */
	/* ------------------------------------------------------------ */
	protected class ClassLoadingObjectInputStream extends ObjectInputStream {
		/* ------------------------------------------------------------ */
		public ClassLoadingObjectInputStream(java.io.InputStream in) throws IOException {
			super(in);
		}

		/* ------------------------------------------------------------ */
		public ClassLoadingObjectInputStream() throws IOException {
			super();
		}

		/* ------------------------------------------------------------ */
		@Override
		public Class<?> resolveClass(java.io.ObjectStreamClass cl) throws IOException, ClassNotFoundException {
			try {
				return Class.forName(cl.getName(), false, Thread.currentThread().getContextClassLoader());
			} catch (ClassNotFoundException e) {
				return super.resolveClass(cl);
			}
		}
	}

}
