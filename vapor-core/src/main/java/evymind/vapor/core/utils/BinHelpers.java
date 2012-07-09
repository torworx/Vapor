package evymind.vapor.core.utils;

import java.util.UUID;

import evymind.vapor.core.RemotingException;
import evymind.vapor.core.VaporBuffer;


public class BinHelpers {
	
	public static void unexpectedEndOfStream() {
		throw new RemotingException("BinHelpers : Unexpected end of buffer");
	}

	public static void unexpectedStringLength() {
		throw new RemotingException("BinHelpers : Invalid string length read from buffer");
	}

	public static void unexpectedBinaryLength() {
		throw new RemotingException("BinHelpers : Invalid binary length read from buffer");
	}

	public static void invalidBufferSize() {
		throw new RemotingException("BinHelpers : Invalid buffer size passed");
	}
	
	public static void writeUUIDToBuffer(UUID uuid, VaporBuffer buffer) {
		buffer.writeBinary(uuidToByteArray(uuid));
	}
	
	public static UUID readUUIDFromBuffer(VaporBuffer buffer) {
		return uuidFromByteArray(buffer.readBinary());
	}
	
	public static byte[] uuidToByteArray(UUID uuid) {
		byte bytesOriginal[] = uuidToBytes(uuid);
		byte[] bytes = new byte[16];

		// Reverse the first 4 bytes
		bytes[0] = bytesOriginal[3];
		bytes[1] = bytesOriginal[2];
		bytes[2] = bytesOriginal[1];
		bytes[3] = bytesOriginal[0];
		// Reverse 6th and 7th
		bytes[4] = bytesOriginal[5];
		bytes[5] = bytesOriginal[4];
		// Reverse 8th and 9th
		bytes[6] = bytesOriginal[7];
		bytes[7] = bytesOriginal[6];
		// Copy the rest straight up
		for (int i = 8; i < 16; i++) {
			bytes[i] = bytesOriginal[i];
		}

		return bytes;
	}

	private static byte[] uuidToBytes(UUID uuid) {
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		byte[] buffer = new byte[16];

		for (int i = 0; i < 8; i++) {
			buffer[i] = (byte) (msb >>> 8 * (7 - i));
		}
		for (int i = 8; i < 16; i++) {
			buffer[i] = (byte) (lsb >>> 8 * (7 - i));
		}

		return buffer;

	}

	public static UUID uuidFromByteArray(byte bytesOriginal[]) {
		byte[] bytes = new byte[16];

		// Reverse the first 4 bytes
		bytes[0] = bytesOriginal[3];
		bytes[1] = bytesOriginal[2];
		bytes[2] = bytesOriginal[1];
		bytes[3] = bytesOriginal[0];
		// Reverse 6th and 7th
		bytes[4] = bytesOriginal[5];
		bytes[5] = bytesOriginal[4];
		// Reverse 8th and 9th
		bytes[6] = bytesOriginal[7];
		bytes[7] = bytesOriginal[6];
		// Copy the rest straight up
		for (int i = 8; i < 16; i++) {
			bytes[i] = bytesOriginal[i];
		}

		return bytesToUUID(bytes);
	}

	private static UUID bytesToUUID(byte[] byteArray) {
		long msb = 0;
		long lsb = 0;
		for (int i = 0; i < 8; i++)
			msb = (msb << 8) | (byteArray[i] & 0xff);
		for (int i = 8; i < 16; i++)
			lsb = (lsb << 8) | (byteArray[i] & 0xff);
		UUID result = new UUID(msb, lsb);

		return result;
	}
	
}
