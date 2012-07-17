package evymind.vapor.core.utils;

import java.util.zip.*;

import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.VaporBuffers;


public class BinZlib {
	
	private static final int DEFAULT_COMPRESSION_BUFFER_SIZE = 256 * 1024;
	
	private static final ThreadLocal<byte[]> compressionBuffer = new ThreadLocal<byte[]>();
	
	private BinZlib() {
	}
	
	private static byte[] getCurrentCompressionBuffer(int size) {
		byte[] buffer = compressionBuffer.get();
		if ((buffer == null) || (buffer.length != size)) {
			buffer = new byte[size];
			compressionBuffer.set(buffer);
		}
		return buffer;
	}

	public static VaporBuffer zlibDeflateData(VaporBuffer originBuffer) {
		return zlibDeflateData(originBuffer, DEFAULT_COMPRESSION_BUFFER_SIZE);
	}
	
	public static VaporBuffer zlibDeflateData(VaporBuffer originBuffer, int compressionBufferSize) {
		byte[] originData = new byte[originBuffer.readableBytes()];
		originBuffer.readBytes(originData);
		Deflater deflater = new Deflater(9);
		deflater.setInput(originData);
		deflater.finish();
		byte[] cb = getCurrentCompressionBuffer(compressionBufferSize);
		VaporBuffer compressedBuffer = VaporBuffers.dynamicBuffer();
		int count;
		while (!deflater.finished()) {
			count = deflater.deflate(cb);
			compressedBuffer.writeBytes(cb, 0, count);
		}
		return compressedBuffer;
	}
	
	public static VaporBuffer zlibInflateData(VaporBuffer compressedBuffer) {
		return zlibInflateData(compressedBuffer, DEFAULT_COMPRESSION_BUFFER_SIZE);
	}

	public static VaporBuffer zlibInflateData(VaporBuffer compressedBuffer, int compressionBufferSize) {
		byte[] compressedData = new byte[compressedBuffer.readableBytes()];
		compressedBuffer.readBytes(compressedData);
		Inflater inflater = new Inflater();
		inflater.setInput(compressedData);
		byte[] cb = getCurrentCompressionBuffer(compressionBufferSize);
		VaporBuffer originBuffer = VaporBuffers.dynamicBuffer();
		int count;
		while (!inflater.finished()) {
			try {
				count = inflater.inflate(cb);
				originBuffer.writeBytes(cb, 0, count);
			} catch (DataFormatException e) {
				throw new BinZlibException("BinZlib : ".concat(e.getMessage()));
			}
		}
		return originBuffer;
	}

}
