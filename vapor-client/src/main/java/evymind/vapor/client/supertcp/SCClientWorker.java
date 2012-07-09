package evymind.vapor.client.supertcp;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.Buffers;
import evymind.vapor.core.supertcp.SCCommand;
import evymind.vapor.core.supertcp.SuperChannelWorker;

public class SCClientWorker extends SuperChannelWorker {
	
	private static final Logger log = LoggerFactory.getLogger(SCClientWorker.class);
	
	protected BaseSuperTCPChannel channel;
	
	private static int id;
	private TimerTask task;
	private Timer timer;
	private long verifyConnectionPeriodMs = 30000; // 30 seconds
	
	private int lastId;
	
	public SCClientWorker(BaseSuperTCPChannel channel) {
		super();
		this.channel = channel;
	}
	
	public SuperClient getConnection() {
		return (SuperClient) super.getConnection();
	}

	@Override
	protected void processConnect(VaporBuffer buffer) {
		processHandshake(buffer);
		connected();
	}
	
	@Override
	protected void handleAck(int id, boolean oke, int errorNo) {
		if (oke) {
			super.handleAck(id, oke, errorNo);
			return;
		}
		WaitingRequest req = channel.getWaitingRequests().get(id);
		if (req == null) {
			super.handleAck(id, oke, errorNo);
		} else {
			req.setResultErrorCode(errorNo);
			req.getSignal().signal();
		}
	}

	@Override
	public void connected() {
		super.connected();
		if (timer == null) {
			timer = new Timer("SCClentWorker-"+id++, true);
		}
		setCheckPeriod(getVerifyConnectionPeriod());
		channel.connected(this);
	}

	@Override
	public void disconnected() {
		super.disconnected();
		if (task != null) {
			task.cancel();
			task = null;
		}
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		channel.disconnected(this);
	}
	
	@Override
	public int generateId() {
		if (++lastId < 1) {
			lastId = 1;
		}
		return lastId;
	}

	public int getVerifyConnectionPeriod() {
		if (verifyConnectionPeriodMs <= 0)
			return 0;
		return (int) (verifyConnectionPeriodMs / 1000);
	}

	public void setCheckPeriod(int seconds) {
		if (seconds == 0)
			seconds = 30;

		long old_period = this.verifyConnectionPeriodMs;
		long period = seconds * 1000L;
		if (period > 60000)
			period = 60000;
		if (period < 1000)
			period = 1000;
		
		this.verifyConnectionPeriodMs = period;
		if (this.timer != null && (period != old_period || this.task == null)) {
			synchronized (this) {
				if (this.task != null)
					this.task.cancel();
				this.task = new TimerTask() {
					@Override
					public void run() {
						verifyConnection();
					}
				};
				this.timer.schedule(this.task, this.verifyConnectionPeriodMs, this.verifyConnectionPeriodMs);
			}
		}
	}
	
	public void verifyConnection() {
		long now = System.currentTimeMillis();
		if (sequencePing != 0 && getConnection().isConnected()) {
			long idleTime = getPingTimeout() * 1000L;
			if (idleTime > 0 && getAccessed() + idleTime < now) {
				timeout();
			}
		} else {
			long pingTime = getPingFrequency() * 1000L;
			if (pingTime > 0 && getAccessed() + pingTime < now) {
				sendPing();
			}
		}
	}
	
	protected void sendPing() {
		if (log.isDebugEnabled()) {
			log.debug("<---- CMD_PING");
		}
		sequencePing = (int) System.currentTimeMillis();
		VaporBuffer data = Buffers.dynamicBuffer(5);
		data.writeByte(SCCommand.CMD_PING.code());
		data.writeInt(sequencePing);
		getConnection().writeBuffer(data);
	}

	@Override
	protected void incomingData(int id, VaporBuffer data) {
		channel.hasData(id, data);
	}

}
