package evymind.vapor.core.buffer.netty;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.core.SimpleString;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.utils.BinHelpers;
import evymind.vapor.core.utils.DataConstants;
import evymind.vapor.core.utils.UTF8Utils;

public class ChannelBufferWrapper implements ChannelBufferExposer {

	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(ChannelBufferWrapper.class);

	protected ChannelBuffer buffer;

	public ChannelBufferWrapper(final ChannelBuffer buffer) {
		this.buffer = buffer;
	}

	public boolean readBoolean() {
		return readByte() != 0;
	}

	public SimpleString readNullableSimpleString() {
		int b = buffer.readByte();
		if (b == DataConstants.NULL) {
			return null;
		} else {
			return readSimpleStringInternal();
		}
	}

	public String readNullableString() {
		int b = buffer.readByte();
		if (b == DataConstants.NULL) {
			return null;
		} else {
			return readStringInternal();
		}
	}

	public SimpleString readSimpleString() {
		return readSimpleStringInternal();
	}

	private SimpleString readSimpleStringInternal() {
		int len = buffer.readInt();
		if (len > buffer.readableBytes()) {
			BinHelpers.unexpectedStringLength();
		}
		return doReadSimpleStringInternal(len);
	}

	private SimpleString doReadSimpleStringInternal(int len) {
		byte[] data = new byte[len];
		buffer.readBytes(data);
		return new SimpleString(data);
	}

	public String readString() {
		return readStringInternal();
	}

	private String readStringInternal() {
		int len = buffer.readInt();

		if (len < 9) {
			char[] chars = new char[len];
			for (int i = 0; i < len; i++) {
				chars[i] = (char) buffer.readShort();
			}
			return new String(chars);
		} else if (len < 0xfff) {
			return readUTF();
		} else {
			return readSimpleStringInternal().toString();
		}
	}

	public String readUTF() {
		return UTF8Utils.readUTF(this);
	}

	public byte[] readBinary() {
		int len = buffer.readInt();
		if (len > buffer.readableBytes()) {
			BinHelpers.unexpectedBinaryLength();
		}
		return readBinary(len);
	}

	public byte[] readBinary(int length) {
		byte[] data = new byte[length];
		buffer.readBytes(data);
		return data;
	}

	public void writeBoolean(final boolean val) {
		buffer.writeByte((byte) (val ? -1 : 0));
	}

	public void writeNullableSimpleString(final SimpleString val) {
		if (val == null) {
			buffer.writeByte(DataConstants.NULL);
		} else {
			buffer.writeByte(DataConstants.NOT_NULL);
			writeSimpleStringInternal(val);
		}
	}

	public void writeNullableString(final String val) {
		if (val == null) {
			buffer.writeByte(DataConstants.NULL);
		} else {
			buffer.writeByte(DataConstants.NOT_NULL);
			writeStringInternal(val);
		}
	}

	public void writeSimpleString(final SimpleString val) {
		writeSimpleStringInternal(val);
	}

	private void writeSimpleStringInternal(final SimpleString val) {
		byte[] data = val.getData();
		buffer.writeInt(data.length);
		buffer.writeBytes(data);
	}

	public void writeString(final String val) {
		writeStringInternal(val);
	}

	private void writeStringInternal(final String val) {
		int length = val.length();

		buffer.writeInt(length);

		if (length < 9) {
			// If very small it's more performant to store char by char
			for (int i = 0; i < val.length(); i++) {
				buffer.writeShort((short) val.charAt(i));
			}
		} else if (length < 0xfff) {
			// Store as UTF - this is quicker than char by char for most strings
			writeUTF(val);
		} else {
			// Store as SimpleString, since can't store utf > 0xffff in length
			writeSimpleStringInternal(new SimpleString(val));
		}
	}

	public void writeUTF(final String utf) {
		UTF8Utils.saveUTF(this, utf);
	}

	public void writeBinary(final byte[] val) {
		buffer.writeInt(val.length);
		buffer.writeBytes(val);
	}

	public int capacity() {
		return buffer.capacity();
	}

	public ChannelBuffer channelBuffer() {
		return buffer;
	}

	public void clear() {
		buffer.clear();
	}

	public VaporBuffer copy() {
		return new ChannelBufferWrapper(buffer.copy());
	}

	public VaporBuffer copy(final int index, final int length) {
		return new ChannelBufferWrapper(buffer.copy(index, length));
	}

	public void discardReadBytes() {
		buffer.discardReadBytes();
	}

	public VaporBuffer duplicate() {
		return new ChannelBufferWrapper(buffer.duplicate());
	}

	public byte getByte(final int index) {
		return buffer.getByte(index);
	}

	public void getBytes(final int index, final byte[] dst, final int dstIndex, final int length) {
		buffer.getBytes(index, dst, dstIndex, length);
	}

	public void getBytes(final int index, final byte[] dst) {
		buffer.getBytes(index, dst);
	}

	public void getBytes(final int index, final ByteBuffer dst) {
		buffer.getBytes(index, dst);
	}

	public void getBytes(final int index, final VaporBuffer dst, final int dstIndex, final int length) {
		buffer.getBytes(index, ((ChannelBufferExposer) dst).channelBuffer(), dstIndex, length);
	}

	public void getBytes(final int index, final VaporBuffer dst, final int length) {
		buffer.getBytes(index, ((ChannelBufferExposer) dst).channelBuffer(), length);
	}

	public void getBytes(final int index, final VaporBuffer dst) {
		buffer.getBytes(index, ((ChannelBufferExposer) dst).channelBuffer());
	}

	public char getChar(final int index) {
		return (char) buffer.getShort(index);
	}

	public double getDouble(final int index) {
		return Double.longBitsToDouble(buffer.getLong(index));
	}

	public float getFloat(final int index) {
		return Float.intBitsToFloat(buffer.getInt(index));
	}

	public int getInt(final int index) {
		return buffer.getInt(index);
	}

	public long getLong(final int index) {
		return buffer.getLong(index);
	}

	public short getShort(final int index) {
		return buffer.getShort(index);
	}

	public short getUnsignedByte(final int index) {
		return buffer.getUnsignedByte(index);
	}

	public long getUnsignedInt(final int index) {
		return buffer.getUnsignedInt(index);
	}

	public int getUnsignedShort(final int index) {
		return buffer.getUnsignedShort(index);
	}

	public void markReaderIndex() {
		buffer.markReaderIndex();
	}

	public void markWriterIndex() {
		buffer.markWriterIndex();
	}

	public boolean readable() {
		return buffer.readable();
	}

	public int readableBytes() {
		return buffer.readableBytes();
	}

	public byte readByte() {
		return buffer.readByte();
	}

	public void readBytes(final byte[] dst, final int dstIndex, final int length) {
		buffer.readBytes(dst, dstIndex, length);
	}

	public void readBytes(final byte[] dst) {
		buffer.readBytes(dst);
	}

	public void readBytes(final ByteBuffer dst) {
		buffer.readBytes(dst);
	}

	public void readBytes(final VaporBuffer dst, final int dstIndex, final int length) {
		buffer.readBytes(((ChannelBufferExposer) dst).channelBuffer(), dstIndex, length);
	}

	public void readBytes(final VaporBuffer dst, final int length) {
		buffer.readBytes(((ChannelBufferExposer) dst).channelBuffer(), length);
	}

	public void readBytes(final VaporBuffer dst) {
		buffer.readBytes(((ChannelBufferExposer) dst).channelBuffer());
	}

	public VaporBuffer readBytes(final int length) {
		return new ChannelBufferWrapper(buffer.readBytes(length));
	}

	public char readChar() {
		return (char) buffer.readShort();
	}

	public double readDouble() {
		return Double.longBitsToDouble(buffer.readLong());
	}

	public int readerIndex() {
		return buffer.readerIndex();
	}

	public void readerIndex(final int readerIndex) {
		buffer.readerIndex(readerIndex);
	}

	public float readFloat() {
		return Float.intBitsToFloat(buffer.readInt());
	}

	public int readInt() {
		return buffer.readInt();
	}

	public long readLong() {
		return buffer.readLong();
	}

	public short readShort() {
		return buffer.readShort();
	}

	public VaporBuffer readSlice(final int length) {
		return new ChannelBufferWrapper(buffer.readSlice(length));
	}

	public short readUnsignedByte() {
		return buffer.readUnsignedByte();
	}

	public long readUnsignedInt() {
		return buffer.readUnsignedInt();
	}

	public int readUnsignedShort() {
		return buffer.readUnsignedShort();
	}

	public void resetReaderIndex() {
		buffer.resetReaderIndex();
	}

	public void resetWriterIndex() {
		buffer.resetWriterIndex();
	}

	public void setByte(final int index, final byte value) {
		buffer.setByte(index, value);
	}

	public void setBytes(final int index, final byte[] src, final int srcIndex, final int length) {
		buffer.setBytes(index, src, srcIndex, length);
	}

	public void setBytes(final int index, final byte[] src) {
		buffer.setBytes(index, src);
	}

	public void setBytes(final int index, final ByteBuffer src) {
		buffer.setBytes(index, src);
	}

	public void setBytes(final int index, final VaporBuffer src, final int srcIndex, final int length) {
		buffer.setBytes(index, ((ChannelBufferExposer) src).channelBuffer(), srcIndex, length);
	}

	public void setBytes(final int index, final VaporBuffer src, final int length) {
		buffer.setBytes(index, ((ChannelBufferExposer) src).channelBuffer(), length);
	}

	public void setBytes(final int index, final VaporBuffer src) {
		buffer.setBytes(index, ((ChannelBufferExposer) src).channelBuffer());
	}

	public void setChar(final int index, final char value) {
		buffer.setShort(index, (short) value);
	}

	public void setDouble(final int index, final double value) {
		buffer.setLong(index, Double.doubleToLongBits(value));
	}

	public void setFloat(final int index, final float value) {
		buffer.setInt(index, Float.floatToIntBits(value));
	}

	public void setIndex(final int readerIndex, final int writerIndex) {
		buffer.setIndex(readerIndex, writerIndex);
	}

	public void setInt(final int index, final int value) {
		buffer.setInt(index, value);
	}

	public void setLong(final int index, final long value) {
		buffer.setLong(index, value);
	}

	public void setShort(final int index, final short value) {
		buffer.setShort(index, value);
	}

	public void skipBytes(final int length) {
		buffer.skipBytes(length);
	}

	public VaporBuffer slice() {
		return new ChannelBufferWrapper(buffer.slice());
	}

	public VaporBuffer slice(final int index, final int length) {
		return new ChannelBufferWrapper(buffer.slice(index, length));
	}

	public ByteBuffer toByteBuffer() {
		return buffer.toByteBuffer();
	}

	public ByteBuffer toByteBuffer(final int index, final int length) {
		return buffer.toByteBuffer(index, length);
	}

	public boolean writable() {
		return buffer.writable();
	}

	public int writableBytes() {
		return buffer.writableBytes();
	}

	public void writeByte(final byte value) {
		buffer.writeByte(value);
	}

	public void writeBytes(final byte[] src, final int srcIndex, final int length) {
		buffer.writeBytes(src, srcIndex, length);
	}

	public void writeBytes(final byte[] src) {
		buffer.writeBytes(src);
	}

	public void writeBytes(final ByteBuffer src) {
		buffer.writeBytes(src);
	}

	public void writeBytes(final VaporBuffer src, final int srcIndex, final int length) {
		buffer.writeBytes(((ChannelBufferExposer) src).channelBuffer(), srcIndex, length);
	}

	public void writeBytes(final VaporBuffer src, final int length) {
		buffer.writeBytes(((ChannelBufferExposer) src).channelBuffer(), length);
	}

	public void writeChar(final char chr) {
		buffer.writeShort((short) chr);
	}

	public void writeDouble(final double value) {
		buffer.writeLong(Double.doubleToLongBits(value));
	}

	public void writeFloat(final float value) {
		buffer.writeInt(Float.floatToIntBits(value));
	}

	public void writeInt(final int value) {
		buffer.writeInt(value);
	}

	public void writeLong(final long value) {
		buffer.writeLong(value);
	}

	public int writerIndex() {
		return buffer.writerIndex();
	}

	public void writerIndex(final int writerIndex) {
		buffer.writerIndex(writerIndex);
	}

	public void writeShort(final short value) {
		buffer.writeShort(value);
	}

	@Override
	public byte[] array() {
		return buffer.array();
	}

	@Override
	public String toString() {
		return buffer.toString();
	}

}
