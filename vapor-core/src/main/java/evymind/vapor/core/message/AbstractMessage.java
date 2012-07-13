package evymind.vapor.core.message;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import evyframework.common.Assert;

import evyframework.common.ClassUtils;
import evymind.vapor.core.Message;
import evymind.vapor.core.VaporRuntimeException;
import evymind.vapor.core.Transport;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.exception.UnregisteredServerException;
import evymind.vapor.core.message.envelope.MessageEnvelope;
import evymind.vapor.core.message.envelope.MessageEnvelopes;
import evymind.vapor.core.serializer.Serializer;

public abstract class AbstractMessage<S extends Serializer> implements Message {

	protected S serializer;
	private UUID clientId;
	private String interfaceName;
	private String messageName;

	private MessageEnvelopes envelopes;

	public AbstractMessage() {
		super();
		initObjects();
	}

	protected void initObjects() {
		serializer = createSerializer();
		clientId = UUID.randomUUID();
		envelopes = new MessageEnvelopes();
	}

	protected abstract S createSerializer();

	protected void envelopesProcessIncoming(VaporBuffer buffer) {
		// TODO BeforeProcessIncomingEnvelopes event
		VaporBuffer _buffer = buffer;
		try {
			String envelopeName = checkForEnvelope(_buffer);
			while (envelopeName != null) {
				MessageEnvelope envelope = envelopes.get(envelopeName);
				Assert.notNull(envelope, envelopeName + " envelope isn't found");
				VaporBuffer processedBuffer = envelope.processIncoming(this, _buffer);
				if (processedBuffer != _buffer) {
					_buffer = processedBuffer;
				}
				envelopeName = checkForEnvelope(_buffer);
			}
		} finally {
			if (_buffer != buffer) {
				buffer.clear();
				buffer.writeBytes(_buffer, _buffer.readableBytes());
			}
		}
		// TODO AfterProcessIncomingEnvelopes event
	}

	protected void envelopesProcessOutcoming(VaporBuffer buffer) {
		// TODO BeforeProcessOutcomingEnvelopes event
		VaporBuffer _buffer = buffer;
		try {
			for (MessageEnvelope envelope : envelopes.values()) {
				VaporBuffer processedBuffer = envelope.processOutcoming(this, _buffer);
				if (processedBuffer != _buffer) {
					_buffer = processedBuffer;
				}
			}
		} finally {
			if (_buffer != buffer) {
				buffer.clear();
				buffer.writeBytes(_buffer, _buffer.readableBytes());
			}
		}
		// TODO AfterProcessOutcomingEnvelopes event
	}

	protected String checkForEnvelope(VaporBuffer buffer) {
		buffer.markReaderIndex();
		if (buffer.readableBytes() > Messages.ENVELOPE_SIGNATURE.length) {
			byte[] buf = new byte[Messages.ENVELOPE_SIGNATURE.length];
			buffer.readBytes(buf);
			boolean envMatched = true;
			for (int i = 0; i < Messages.ENVELOPE_SIGNATURE.length; i++) {
				if (buf[i] != Messages.ENVELOPE_SIGNATURE[i]) {
					envMatched = false;
					break;
				}
			}
			try {
				if (envMatched) {
					return buffer.readUTF();
				}
			} catch (Exception e) {
			}
			buffer.resetReaderIndex();
		}
		return null;
	}

	public void writeEnvelopeHeader(MessageEnvelope envelope, VaporBuffer buffer) {
		buffer.writeBytes(Messages.ENVELOPE_SIGNATURE);
		buffer.writeUTF(envelope.getEnvelopeMarker());
	}

	@Override
	public void initializeMessage(Transport transport, String interfaceName, String messageName, MessageType messageType) {
		this.interfaceName = interfaceName;
		this.messageName = messageName;
	}

	@Override
	public void initializeMessage(Transport transport, String libraryName, String interfaceName, String messageName,
			MessageType messageType) {
		initializeMessage(transport, interfaceName, messageName, messageType);
	}

	@Override
	public void initializeRequestMessage(Transport transport, String libraryName, String interfaceName,
			String messageName) {
		initializeMessage(transport, libraryName, interfaceName, messageName, MessageType.REQUEST);
	}

	@Override
	public void initializeResponseMessage(Transport transport, String libraryName, String interfaceName,
			String messageName) {
		initializeMessage(transport, libraryName, interfaceName, messageName, MessageType.RESPONSE);
	}

	@Override
	public void initializeEventMessage(Transport transport, String libraryName, String interfaceName, String messageName) {
		initializeMessage(transport, libraryName, interfaceName, messageName, MessageType.EVENT);
	}

	@Override
	public void initializeExceptionMessage(Transport transport, String libraryName, String interfaceName,
			String messageName) {
		// do nothing here; descendents can override this to set the types
	}

	@Override
	public void finalizeMessage() {
		// no-op
	}

	@Override
	public void initializeRead(Transport transport) {
		// no-op
	}

	public void readFromBuffer(VaporBuffer buffer) {
		envelopesProcessIncoming(buffer);
		Assert.isTrue(buffer.readableBytes() > 4, "Message : The data is too short to be a message");
		doReadFromeBuffer(buffer);
	}

	protected abstract void doReadFromeBuffer(VaporBuffer buffer);

	@Override
	public void writeToBuffer(VaporBuffer buffer) {
		doWriteToBuffer(buffer);
		envelopesProcessOutcoming(buffer);
	}

	protected abstract void doWriteToBuffer(VaporBuffer buffer);

	protected void processException() {
		RuntimeException exception = readException();
		if (exception != null) {
			throw exception;
		}
	}

	protected abstract RuntimeException readException();

	@SuppressWarnings("unchecked")
	protected RuntimeException createException(final String exceptionName, final String message) {
		Class<?> exceptionClass = ClassUtils.resolveClassName(exceptionName, ClassUtils.getDefaultClassLoader());
		if (exceptionClass != null) {
			try {
				Constructor<RuntimeException> constructor = (Constructor<RuntimeException>) exceptionClass.getConstructor(String.class);
				return constructor.newInstance(message);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new VaporRuntimeException(e);
			}
		} else {
			return new UnregisteredServerException(exceptionName, message);
		}
	}

	public <T> T read(String name, Class<T> classOfT) {
		return serializer.read(name, classOfT);
	}

	public BigDecimal readBigDecimal(String name) {
		return serializer.readBigDecimal(name);
	}

	public BigInteger readBigInteger(String name) {
		return serializer.readBigInteger(name);
	}

	public byte[] readBinary(String name) {
		return serializer.readBinary(name);
	}

	public boolean readBoolean(String name) {
		return serializer.readBoolean(name);
	}

	public byte readByte(String name) {
		return serializer.readByte(name);
	}

	public char readChar(String name) {
		return serializer.readChar(name);
	}

	public Date readDate(String name) {
		return serializer.readDate(name);
	}

	public double readDouble(String name) {
		return serializer.readDouble(name);
	}

	public float readFloat(String name) {
		return serializer.readFloat(name);
	}

	public int readInteger(String name) {
		return serializer.readInteger(name);
	}

	public long readLong(String name) {
		return serializer.readLong(name);
	}

	public <T> T readObject(String name) {
		return serializer.readObject(name);
	}

	public <T> void readObject(String name, T object) {
		serializer.readObject(name, object);
	}

	public short readShort(String name) {
		return serializer.readShort(name);
	}

	public String readString(String name) {
		return serializer.readString(name);
	}

	public Timestamp readTimestamp(String name) {
		return serializer.readTimestamp(name);
	}

	public String readUTF(String name) {
		return serializer.readUTF(name);
	}

	public <T> void write(String name, T object) {
		serializer.write(name, object);
	}

	@Override
	public <T> void write(String name, Object value, Class<T> type) {
		serializer.write(name, value, type);
	}

	public void writeBigDecimal(String name, BigDecimal v) {
		serializer.writeBigDecimal(name, v);
	}

	public void writeBigInteger(String name, BigInteger v) {
		serializer.writeBigInteger(name, v);
	}

	public void writeBoolean(String name, boolean v) {
		serializer.writeBoolean(name, v);
	}

	public void writeByinary(String name, byte[] b) {
		serializer.writeBinary(name, b);
	}

	public void writeByte(String name, byte v) {
		serializer.writeByte(name, v);
	}

	public void writeChar(String name, char v) {
		serializer.writeChar(name, v);
	}

	public void writeDate(String name, Date v) {
		serializer.writeDate(name, v);
	}

	public void writeDouble(String name, double v) {
		serializer.writeDouble(name, v);
	}

	public void writeFloat(String name, float v) {
		serializer.writeFloat(name, v);
	}

	public void writeInteger(String name, int v) {
		serializer.writeInteger(name, v);
	}

	public void writeLong(String name, long v) {
		serializer.writeLong(name, v);
	}

	public <T> void writeObject(String name, T object) {
		serializer.writeObject(name, object);
	}

	public void writeShort(String name, short v) {
		serializer.writeShort(name, v);
	}

	public void writeString(String name, String v) {
		serializer.writeString(name, v);
	}

	public void writeTimestamp(String name, Timestamp v) {
		serializer.writeTimestamp(name, v);
	}

	public void writeUTF(String name, String v) {
		serializer.writeUTF(name, v);
	}

	public UUID getClientId() {
		return clientId;
	}

	public void setClientId(UUID clientId) {
		this.clientId = clientId;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getMessageName() {
		return messageName;
	}

	public void setMessageName(String messageName) {
		this.messageName = messageName;
	}

	public MessageEnvelopes getEnvelopes() {
		return envelopes;
	}

	public void setEnvelopes(MessageEnvelopes envelopes) {
		this.envelopes.clear();
		if (envelopes != null && !envelopes.isEmpty()) {
			this.envelopes.putAll(envelopes);
		}
	}

}
