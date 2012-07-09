package evymind.vapor.core.utils;

import java.lang.ref.SoftReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.core.VaporBuffer;


/**
 * A UTF8Utils
 * 
 * This class will write UTFs directly to the ByteOutput (through the
 * MessageBuffer interface)
 * 
 */
public class UTF8Utils {
	
	static boolean optimizeStrings = true;

	private static final Logger log = LoggerFactory.getLogger(UTF8Utils.class);

	private static final boolean isTrace = UTF8Utils.log.isTraceEnabled();

	private static ThreadLocal<SoftReference<StringUtilBuffer>> currenBuffer = new ThreadLocal<SoftReference<StringUtilBuffer>>();

	public static void saveUTF(final VaporBuffer out, final String str) {
		StringUtilBuffer buffer = UTF8Utils.getThreadLocalBuffer();

		if (str.length() > 0xffff) {
			throw new IllegalArgumentException("the specified string is too long (" + str.length() + ")");
		}

		final int len = UTF8Utils.calculateUTFSize(str, buffer);

		if (len > 0xffff) {
			throw new IllegalArgumentException("the encoded string is too long (" + len + ")");
		}

		out.writeShort((short) len);

		if (len > buffer.byteBuffer.length) {
			buffer.resizeByteBuffer(len);
		}

		if (len == (long) str.length()) {
			for (int byteLocation = 0; byteLocation < len; byteLocation++) {
				buffer.byteBuffer[byteLocation] = (byte) buffer.charBuffer[byteLocation];
			}
			out.writeBytes(buffer.byteBuffer, 0, len);
		} else {
			if (UTF8Utils.isTrace) {
				// This message is too verbose for debug, that's why we are
				// using trace here
				UTF8Utils.log.trace("Saving string with utfSize=" + len + " stringSize=" + str.length());
			}

			int stringLength = str.length();

			int charCount = 0;

			for (int i = 0; i < stringLength; i++) {
				char charAtPos = buffer.charBuffer[i];
				if (charAtPos >= 1 && charAtPos < 0x7f) {
					buffer.byteBuffer[charCount++] = (byte) charAtPos;
				} else if (charAtPos >= 0x800) {
					buffer.byteBuffer[charCount++] = (byte) (0xE0 | charAtPos >> 12 & 0x0F);
					buffer.byteBuffer[charCount++] = (byte) (0x80 | charAtPos >> 6 & 0x3F);
					buffer.byteBuffer[charCount++] = (byte) (0x80 | charAtPos >> 0 & 0x3F);
				} else {
					buffer.byteBuffer[charCount++] = (byte) (0xC0 | charAtPos >> 6 & 0x1F);
					buffer.byteBuffer[charCount++] = (byte) (0x80 | charAtPos >> 0 & 0x3F);

				}
			}
			out.writeBytes(buffer.byteBuffer, 0, len);
		}
	}

	public static String readUTF(final VaporBuffer input) {
		StringUtilBuffer buffer = UTF8Utils.getThreadLocalBuffer();

		final int size = input.readUnsignedShort();
		
		if (size > input.readableBytes()) {
			BinHelpers.unexpectedStringLength();
		}

		if (size > buffer.byteBuffer.length) {
			buffer.resizeByteBuffer(size);
		}

		if (size > buffer.charBuffer.length) {
			buffer.resizeCharBuffer(size);
		}

		if (UTF8Utils.isTrace) {
			// This message is too verbose for debug, that's why we are using
			// trace here
			UTF8Utils.log.trace("Reading string with utfSize=" + size);
		}

		int count = 0;
		int byte1, byte2, byte3;
		int charCount = 0;

		input.readBytes(buffer.byteBuffer, 0, size);

		while (count < size) {
			byte1 = buffer.byteBuffer[count++];

			if (byte1 > 0 && byte1 <= 0x7F) {
				buffer.charBuffer[charCount++] = (char) byte1;
			} else {
				int c = byte1 & 0xff;
				switch (c >> 4) {
				case 0xc:
				case 0xd:
					byte2 = buffer.byteBuffer[count++];
					buffer.charBuffer[charCount++] = (char) ((c & 0x1F) << 6 | byte2 & 0x3F);
					break;
				case 0xe:
					byte2 = buffer.byteBuffer[count++];
					byte3 = buffer.byteBuffer[count++];
					buffer.charBuffer[charCount++] = (char) ((c & 0x0F) << 12 | (byte2 & 0x3F) << 6 | (byte3 & 0x3F) << 0);
					break;
				}
			}
		}

		return new String(buffer.charBuffer, 0, charCount);

	}

	private static StringUtilBuffer getThreadLocalBuffer() {
		SoftReference<StringUtilBuffer> softReference = UTF8Utils.currenBuffer.get();
		StringUtilBuffer value;
		if (softReference == null) {
			value = new StringUtilBuffer();
			softReference = new SoftReference<StringUtilBuffer>(value);
			UTF8Utils.currenBuffer.set(softReference);
		} else {
			value = softReference.get();
		}

		if (value == null) {
			value = new StringUtilBuffer();
			softReference = new SoftReference<StringUtilBuffer>(value);
			UTF8Utils.currenBuffer.set(softReference);
		}

		return value;
	}

	public static void clearBuffer() {
		SoftReference<StringUtilBuffer> ref = UTF8Utils.currenBuffer.get();
		if (ref.get() != null) {
			ref.clear();
		}
	}

	public static int calculateUTFSize(final String str, final StringUtilBuffer stringBuffer) {
		int calculatedLen = 0;

		int stringLength = str.length();

		if (stringLength > stringBuffer.charBuffer.length) {
			stringBuffer.resizeCharBuffer(stringLength);
		}

		str.getChars(0, stringLength, stringBuffer.charBuffer, 0);

		for (int i = 0; i < stringLength; i++) {
			char c = stringBuffer.charBuffer[i];

			if (c >= 1 && c < 0x7f) {
				calculatedLen++;
			} else if (c >= 0x800) {
				calculatedLen += 3;
			} else {
				calculatedLen += 2;
			}
		}
		return calculatedLen;
	}

	private static class StringUtilBuffer {

		public char charBuffer[];

		public byte byteBuffer[];

		public void resizeCharBuffer(final int newSize) {
			if (newSize > charBuffer.length) {
				charBuffer = new char[newSize];
			}
		}

		public void resizeByteBuffer(final int newSize) {
			if (newSize > byteBuffer.length) {
				byteBuffer = new byte[newSize];
			}
		}

		public StringUtilBuffer() {
			this(1024, 1024);
		}

		public StringUtilBuffer(final int sizeChar, final int sizeByte) {
			charBuffer = new char[sizeChar];
			byteBuffer = new byte[sizeByte];
		}

	}

}
