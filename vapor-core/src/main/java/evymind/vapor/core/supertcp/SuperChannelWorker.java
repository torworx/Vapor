package evymind.vapor.core.supertcp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

import evyframework.common.Assert;

import evymind.vapor.core.MessageTooLargeException;
import evymind.vapor.core.QueueFullException;
import evymind.vapor.core.VaporRuntimeException;
import evymind.vapor.core.TimeoutException;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.Buffers;
import evymind.vapor.core.utils.AsciiUtils;
import evymind.vapor.core.utils.BinHelpers;
import evymind.vapor.core.utils.UuidUtils;

public abstract class SuperChannelWorker {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	/**
	 * <pre>
	 * Client: 
	 *   "ERSC10" 
	 *   [MyUUID] or [NullUUID] 
	 *   MaxPackageLength: Int32
	 * Server:
	 *   "ERSC10"
	 *   [YourUUID] or [NewUUID]
	 *   MaxPackageLength: Int32;
	 * </pre>
	 */
	public static final byte[] WELCOME = AsciiUtils.toAsciiBytes("ERSC10");
	
	protected SuperConnection connection;
	
	protected final DataReceiver dataReceiver = new DataReceiver();

	protected int pingFrequency = 60;
	protected int pingTimeout = 90;
	protected int maxPackageSize = 10 * 1024 * 1024; // 10m
	
	protected UUID clientId;
	protected boolean skipAck;
	
	protected int remoteMaxPackageSize;
	protected boolean remoteSkipAck;
	protected boolean remoteSupportsOptions;
	
	protected boolean connected;
	
	protected long accessed;
	protected long lastAccessed;
	
	protected int sequencePing;
	
	private Map<Integer, PackageAck> waitingAcks = new HashMap<Integer, PackageAck>();
	
	public SuperChannelWorker() {
		this(null);
	}

	public SuperChannelWorker(SuperConnection connection) {
		this.connection = connection;
	}
	
	public static void waitForAck(PackageAck ack, int timeout) {
		if (ack.getSignal() == null) { //skipack
			return;
		}
		
		try {
			if (!ack.getSignal().await(timeout)) {
				throw new TimeoutException("Timeout");
			}
		} catch (InterruptedException e) {
			throw new VaporRuntimeException(e);
		}
		
		if (ack.getAckState() != AckState.ACK) {
			switch (ack.getAckError()) {
			case PackageAck.NOACK_MESSAGE_TOO_LARGE:
				throw new MessageTooLargeException("Message too large");
			case PackageAck.NOACK_QUEUE_FULL:
				throw new QueueFullException("Queue full");
			case PackageAck.NOACK_TIMEOUT:
				throw new VaporRuntimeException("Timeout");
			case PackageAck.NOACK_UNKNOWN_COMMAND:
				throw new VaporRuntimeException("Unknown command");
			default:
				break;
			}
		}
	}

	public final void process(VaporBuffer buffer) {
		this.access(System.currentTimeMillis());
		if (!isConnected()) {
			processConnect(buffer);
		} else {
			doProcess(buffer);
		}
	}
	
	protected abstract void processConnect(VaporBuffer buffer);
	
	protected boolean processHandshake(VaporBuffer buffer) {
		byte[] cmd = buffer.readBinary(WELCOME.length);
		if (!Arrays.equals(WELCOME, cmd)) {
			if (log.isDebugEnabled()) {
				log.debug("--> = !Welcome");
				log.debug("<---- DefaultResponse");
				log.debug("===== Disconnect");
			}
			try {
				getConnection().writeString(getDefaultResponse());
			} catch (Exception e) {
				disconnect();
			}
			return false;
		}
		
		log.debug("--> Welcome");
		
		// informations
		UUID uuid = BinHelpers.uuidFromByteArray(buffer.readBinary(16));
		if (UuidUtils.EMPTY_UUID.equals(uuid)) {
			uuid = UUID.randomUUID();
		}
		this.clientId = uuid;
		this.remoteMaxPackageSize = buffer.readInt();
		return true;
	}
	
	public void sendHandshake() {
		log.debug("<---- Welcome");
		VaporBuffer buf = Buffers.dynamicBuffer();
		buf.writeBytes(WELCOME);
		buf.writeBytes(BinHelpers.uuidToByteArray(getClientId() == null ? UuidUtils.EMPTY_UUID : getClientId()));
		buf.writeInt(getMaxPackageSize());
		getConnection().writeBuffer(buf);
	}
	
	protected final void doProcess(VaporBuffer buffer) {
		while (buffer.readable()) {
			
			if (dataReceiver.isInProgress()) {
				dataReceiver.receive(buffer);
			};
			
			if (!buffer.readable()) {
				return;
			}
			
			byte cmdCode = buffer.readByte();
			SCCommand cmd = SCCommand.fromCode(cmdCode);
			if (cmd == null) {
				log.warn("Unknown command code ({})", cmdCode);
				return;
			}
			log.debug("--> " + cmd);
			// check max package size
			if (buffer.writerIndex() > getMaxPackageSize()) {
				if (log.isDebugEnabled()) {
					log.debug("----- req.size > maxPackageSize");
					log.debug("<---- CMD_NO_ACK ");
					log.debug("===== Disconnect");
				}
				sendNoAck(PackageAck.NOACK_MESSAGE_TOO_LARGE);
				disconnect();
			}
			
			int id;
			
			switch (cmd) {
				case CMD_PONG:
					if (buffer.readInt() == sequencePing) {
						sequencePing = 0;
						if (log.isDebugEnabled()) {
							log.debug("----> sequencePing = 0");
						}
					}
					break;
				case CMD_PING:
					if (log.isDebugEnabled()) {
						log.debug("<---- CMD_PONG");
					}
					VaporBuffer resp = Buffers.dynamicBuffer();
					resp.writeByte(SCCommand.CMD_PONG.code());
					resp.writeInt(buffer.readInt());
					getConnection().writeBuffer(resp);
					break;
				case CMD_ACK:
					handleAck(buffer.readInt(), true, 0);
					break;
				case CMD_NO_ACK:
					id = buffer.readInt();
					byte c = buffer.readByte();
					if (c == PackageAck.NOACK_SUPPORTS_OPTIONS) {
						if (skipAck) {
							sendOptions(Option.SKIPACK.on().toString());
						}
						remoteSupportsOptions = true;
					} else {
						handleAck(id, false, c);
					}
					break;
				case CMD_OPTIONS: 
					handleOption(buffer.readUTF());
					break;
				case CMD_PACKAGE: 
					final int packageId = buffer.readInt();
					int len = buffer.readInt(); 
					if (len < 0) {
						if (log.isDebugEnabled()) {
							log.debug("----> len < 0");
							log.debug("<---- CMD_NO_ACK UnknownCommand");
							log.debug("===== Disconnect");
						}
						sendNoAck(PackageAck.NOACK_UNKNOWN_COMMAND);
						disconnect();
					}
					try {
						dataReceiver.start(len, new DataTransferListener() {
							@Override
							public void receiveProgress(DataTransferState state, int transfered, int total) {
								log.debug("Receiving package [id={}]: {} {}%", new Object[]{packageId, state.toString(), transfered * 100 / total});
							}
							@Override
							public void receiveComplete(VaporBuffer data) {
								incomingData(packageId, data);
								
								// Send CMD_ACK when processed completely
								if (!remoteSkipAck) {
									if (log.isDebugEnabled()) {
										log.debug("<---- CMD_ACK");
									}
									sendAck(packageId);
								}
							}
						});
						
						dataReceiver.receive(buffer);
					} catch (Exception e) {
						if (log.isDebugEnabled()) {
							log.debug("<---- CMD_NO_ACK QueueFull");
						}
						sendNoAck(packageId, PackageAck.NOACK_QUEUE_FULL);
					}
					break;
				default:
					return;
			}
		}
	}
	
	protected void handleAck(int id, boolean oke, int errorNo) {
		if (log.isDebugEnabled()) {
			log.debug("waiting acks count = " + waitingAcks.size());
		}
		if (waitingAcks.containsKey(id)) {
			PackageAck ack = waitingAcks.get(id);
			waitingAcks.remove(id);
			ack.setAckError(errorNo);
			ack.setAckState(oke ? AckState.ACK : AckState.NO_ACK);
			ack.getSignal().signal();
			log.debug("signal waiting ack({})", id);
		}
	}
	
	protected void sendOptions(String options) {
		if (log.isDebugEnabled()) {
			log.debug("<---- CMD_OPTIONS");
		}
		VaporBuffer buffer = Buffers.dynamicBuffer();
		buffer.writeByte(SCCommand.CMD_OPTIONS.code());
		buffer.writeUTF(options);
		getConnection().writeBuffer(buffer);
	}
	
	protected void handleOption(String data) {
		Option option = Option.parse(data);
		if (Option.SKIPACK.equals(option)) {
			remoteSkipAck = option.isOn();
		}
	}
	
	protected void sendAck(int id) {
		access(System.currentTimeMillis());
		VaporBuffer data = Buffers.dynamicBuffer();
		data.writeByte(SCCommand.CMD_ACK.code());
		data.writeInt(id);
		getConnection().writeBuffer(data);
	}
	
	protected void sendNoAck(byte error) {
		sendNoAck(0, error);
	}
	
	protected void sendNoAck(int id, byte error) {
		access(System.currentTimeMillis());
		VaporBuffer data = Buffers.dynamicBuffer();
		data.writeByte(SCCommand.CMD_NO_ACK.code());
		data.writeInt(id);
		data.writeByte(error);
		getConnection().writeBuffer(data);
	}
	
	public void sendError(int id, byte error) {
		sendNoAck(id, error);
	}
	
	public PackageAck sendPackage(int id, VaporBuffer data) {
		log.debug("Sending package to {}, id={}", getClientId(), id);
		PackageAck ack = null;
		if (getMaxPackageSize() < data.writerIndex()) {
			throw new VaporRuntimeException("Package too large");
		} else {
			if (id == 0) id = generateId();
			ack = sendData(id, data);
		}
		log.debug("Sended package to {}, id={}", getClientId(), id);
		return ack;
	}
	
	protected synchronized PackageAck sendData(int id, VaporBuffer data) {
		access(System.currentTimeMillis());
		PackageAck answer = new StandardPackageAck(this, id);
		waitingAcks.put(id, answer);
		VaporBuffer buffer = Buffers.dynamicBuffer(4096);
		buffer.writeByte(SCCommand.CMD_PACKAGE.code());
		buffer.writeInt(id);
		buffer.writeInt(data.readableBytes());
		// TODO: progress
		int count;
		while (data.readable()) {
			count = buffer.writableBytes() < data.readableBytes() ? buffer.writableBytes() : data.readableBytes();
			buffer.writeBytes(data, count);
			getConnection().writeBuffer(buffer); 
			buffer.clear();
		}
		return answer;
	}
	
	protected abstract void incomingData(int id, VaporBuffer data);
	
	public SuperConnection getConnection() {
		return this.connection;
	}

	public void setConnection(SuperConnection connection) {
		this.connection = connection;
	}

	public boolean isConnected() {
		return this.connected;
	}
	
	public void disconnect() {
		log.debug("Disconnect");
		this.connection.disconnect();
	}
	
	public void connected() {
		log.debug("*** Connected");
		this.connected = true;
		sendNoAckSupportsOptions();
	}
	
	protected void sendNoAckSupportsOptions() {
		if (log.isDebugEnabled()) {
			log.debug("<---- NOACK_SUPPORTS_OPTIONS");
		}
		VaporBuffer buffer = Buffers.dynamicBuffer();
		buffer.writeByte(SCCommand.CMD_NO_ACK.code());
		buffer.writeInt(0);
		buffer.writeByte(PackageAck.NOACK_SUPPORTS_OPTIONS);
		getConnection().writeBuffer(buffer);
	}
	
	public void disconnected() {
		log.debug("*** Disconnected");
		this.connected = false;
	}
	
	public abstract int generateId();
	

	public String getDefaultResponse() {
		return "ERSC: Invalid connection string";
	}
	
	public void access(long time) {
		synchronized (this) {
			this.lastAccessed = this.accessed;
			this.accessed = time;
		}
	}
	
	public void timeout() {
		if (log.isDebugEnabled()) {
			log.debug("<---- CMD_NO_ACK Timeout");
			log.debug("===== Disconnect");
		}
		sendNoAck(PackageAck.NOACK_TIMEOUT);
		disconnect();
	}
	
	public long getAccessed() {
		return accessed;
	}

	public long getLastAccessed() {
		return lastAccessed;
	}

	public int getPingFrequency() {
		return pingFrequency;
	}

	public void setPingFrequency(int pingFrequency) {
		this.pingFrequency = pingFrequency;
	}

	public int getPingTimeout() {
		return pingTimeout;
	}

	public void setPingTimeout(int pingTimeout) {
		this.pingTimeout = pingTimeout;
	}

	public int getMaxPackageSize() {
		return maxPackageSize;
	}

	public void setMaxPackageSize(int maxPackageSize) {
		this.maxPackageSize = maxPackageSize;
	}

	public boolean isSkipAck() {
		return skipAck;
	}

	public void setSkipAck(boolean skipAck) {
		this.skipAck = skipAck;
	}

	public UUID getClientId() {
		return clientId;
	}

	public void setClientId(UUID clientId) {
		this.clientId = clientId;
	}
	
	public int getRemoteMaxPackageSize() {
		return remoteMaxPackageSize;
	}

	public boolean isRemoteSkipAck() {
		return remoteSkipAck;
	}

	public boolean isRemoteSupportsOptions() {
		return remoteSupportsOptions;
	}
	
	public static class Option {
		
		private static final Splitter PROPERTY_SPLITTER = Splitter.on("=");
		
		public static final Option SKIPACK = new Option("SKIPACK");
		
		private final String name;
		private boolean off = false;

		public Option(String name) {
			Assert.hasText(name, "'name' must not be null or blank");
			this.name = name;
		}
		
		public static Option parse(String option) {
			Assert.hasText(option, "'option' must not be null or blank");
			Iterator<String> values = PROPERTY_SPLITTER.split(option).iterator();
			Option answer = null; 
			if (values.hasNext()) {
				answer = new Option(values.next());
			}
			if (values.hasNext()) {
				if ("OFF".equalsIgnoreCase(values.next())) {
					answer.off();
				} else {
					answer.on();
				}
			}

			return answer;
		}
		
		public Option on() {
			this.off = false;
			return this;
		}
		
		public Option off() {
			this.off = true;
			return this;
		}
		
		public boolean isOn() {
			return !off;
		}
		
		public boolean isOff() {
			return off;
		}
		
		public boolean equals(Option option) {
			return name.equalsIgnoreCase(option.name);
		}
		
		public String toString() {
			return name + "=" + (off ? "OFF" : "ON");
		}
		
		
	}
	
}
