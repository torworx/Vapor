package evymind.vapor.server.supertcp;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import evymind.vapor.core.supertcp.SuperChannelWorker;
import evymind.vapor.core.supertcp.SuperConnection;
import evymind.vapor.core.utils.UuidUtils;
import evymind.vapor.core.utils.component.AbstractLifecycle;

public abstract class ClientManager extends AbstractLifecycle {
	
	private static final Logger log = LoggerFactory.getLogger(ClientManager.class);
	
	private static int id;
	
	protected ClassLoader loader;
	
	private Timer timer;
	private TimerTask task;
	private long scavengePeriodMs = 30000;
	
	private List<UUID> clientIds = Lists.newArrayList();
	private Map<UUID, SuperChannelWorker> clientsMap = Maps.newConcurrentMap();

	@Override
	protected void doStart() throws Exception {
		loader = Thread.currentThread().getContextClassLoader();
		super.doStart();
		
		if (this.timer == null) {
			this.timer = new Timer("ClientScavenger-" + id++, true);
		}

		setScavengePeriod(getScavengePeriod());
	}

	@Override
	protected void doStop() throws Exception {
		// stop the scavengers
		synchronized (this) {
			if (task != null) {
				task.cancel();
				task = null;
			}
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
		}
		
		super.doStop();
		loader = null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends SuperChannelWorker> T createClient(SuperConnection connection) {
		T worker = (T) doCreateClient(connection);
		return worker;
	}
	
	protected abstract SuperChannelWorker doCreateClient(SuperConnection connection);

	public void add(SuperChannelWorker client) {
		if (isRunning()) {
			synchronized (clientIds) {
				clientsMap.put(client.getClientId(), client);
				clientIds.add(client.getClientId());
			}
		}
	}

	public boolean remove(UUID id) {
		if (UuidUtils.isEmpty(id)) {
			return false;
		}
		synchronized (clientIds) {
			clientIds.remove(id);
			return clientsMap.remove(id) != null;
		}
	}
	
	public boolean remove(SuperChannelWorker client) {
		return remove(client.getClientId());
	}
	
	public boolean remove(String id) {
		return remove(UUID.fromString(id));
	}
	
	public List<UUID> getClientIds() {
		return Collections.unmodifiableList(clientIds);
	}
	
	public boolean isValid(SuperChannelWorker client) {
		return clientsMap.containsValue(client);
	}
	
	/**
	 * @return the period in seconds at which a check is made for sessions to be
	 *         invalidated.
	 */
	public int getScavengePeriod() {
		return (int) (this.scavengePeriodMs / 1000);
	}
	
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
	
	protected void scavenge() {
		// don't attempt to scavenge if we are shutting down
		if (isStopping() || isStopped())
			return;

		Thread thread = Thread.currentThread();
		ClassLoader old_loader = thread.getContextClassLoader();
		try {
			if (this.loader != null)
				thread.setContextClassLoader(this.loader);

			// For each worker
			long now = System.currentTimeMillis();
			for (Iterator<SuperChannelWorker> i = this.clientsMap.values().iterator(); i.hasNext();) {
				SuperChannelWorker client = i.next();
				long idleTime = client.getPingTimeout() * 1000L;
				if (idleTime > 0 && client.getAccessed() + idleTime < now) {
					client.timeout();
				}
			}
		} catch (Throwable t) {
			log.warn("Problem scavenging sessions", t);
		} finally {
			thread.setContextClassLoader(old_loader);
		}
	}
}
