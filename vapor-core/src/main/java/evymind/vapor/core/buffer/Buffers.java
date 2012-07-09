package evymind.vapor.core.buffer;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.netty.ChannelBufferWrapper;


/**
 * Factory class to create Buffers
 */
public class Buffers {
	
    /**
     * Creates a new big-endian dynamic buffer whose estimated data length is
     * {@code 256} bytes.  The new buffer's {@code readerIndex} and
     * {@code writerIndex} are {@code 0}.
     */
	public static VaporBuffer dynamicBuffer() {
		return dynamicBuffer(256);
	}
	/**
	 * Creates a <em>self-expanding</em> Buffer with the given initial size
	 * 
	 * @param size
	 *            the initial size of the created Buffer
	 * @return a self-expanding Buffer starting with the given size
	 */
	public static VaporBuffer dynamicBuffer(final int size) {
		return new ChannelBufferWrapper(ChannelBuffers.dynamicBuffer(size));
	}

	/**
	 * Creates a <em>self-expanding</em> Buffer filled with the given byte
	 * array
	 * 
	 * @param bytes
	 *            the created buffer will be initially filled with this byte
	 *            array
	 * @return a self-expanding Buffer filled with the given byte array
	 */
	public static VaporBuffer dynamicBuffer(final byte[] bytes) {
		ChannelBuffer buff = ChannelBuffers.dynamicBuffer(bytes.length);

		buff.writeBytes(bytes);

		return new ChannelBufferWrapper(buff);
	}

	/**
	 * Creates a Buffer wrapping an underlying NIO ByteBuffer
	 * 
	 * The position on this buffer won't affect the position on the inner buffer
	 * 
	 * @param underlying
	 *            the underlying NIO ByteBuffer
	 * @return a Buffer wrapping the underlying NIO ByteBuffer
	 */
	public static VaporBuffer wrappedBuffer(final ByteBuffer underlying) {
		VaporBuffer buff = new ChannelBufferWrapper(ChannelBuffers.wrappedBuffer(underlying));

		buff.clear();

		return buff;
	}

	/**
	 * Creates a Buffer wrapping an underlying byte array
	 * 
	 * @param underlying
	 *            the underlying byte array
	 * @return a Buffer wrapping the underlying byte array
	 */
	public static VaporBuffer wrappedBuffer(final byte[] underlying) {
		return new ChannelBufferWrapper(ChannelBuffers.wrappedBuffer(underlying));
	}

	/**
	 * Creates a <em>fixed</em> Buffer of the given size
	 * 
	 * @param size
	 *            the size of the created Buffer
	 * @return a fixed Buffer with the given size
	 */
	public static VaporBuffer fixedBuffer(final int size) {
		return new ChannelBufferWrapper(ChannelBuffers.buffer(size));
	}

	private Buffers() {
	}
}