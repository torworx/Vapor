package evymind.vapor.core.serializer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

public interface Serializer {

	<T> T read(String name, Class<T> classOfT);
	
	BigDecimal readBigDecimal(String name);

	BigInteger readBigInteger(String name);

	byte[] readBinary(String name);

	boolean readBoolean(String name);

	byte readByte(String name);
	
	char readChar(String name);

	Date readDate(String name);

	double readDouble(String name);
	
	float readFloat(String name);

	int readInteger(String name);

	long readLong(String name);
	
	<T> T readObject(String name);

	<T> void readObject(String name, T object);

	short readShort(String name);
	
	String readString(String name);

	Timestamp readTimestamp(String name);

	String readUTF(String name);

	<T> void write(String name, T value);
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @param type type to serialize as, value can be a string, type is Object.class
	 */
	<T> void write(String name, Object value, Class<T> type);
	
	void writeBigDecimal(String name, BigDecimal v);

	void writeBigInteger(String name, BigInteger v);

	void writeBoolean(String name, boolean v);

	void writeBinary(String name, byte[] b);
	
	void writeByte(String name, byte v);

	void writeChar(String name, char v);
	
	void writeDate(String name, Date v);

	void writeDouble(String name, double v);
	
	void writeFloat(String name, float v);
	
	void writeInteger(String name, int v);
	
	void writeLong(String name, long v);
	
	<T> void writeObject(String name, T object);
	
	void writeShort(String name, short v);
	
	void writeString(String name, String v);
	
	void writeTimestamp(String name, Timestamp v);
	
	void writeUTF(String name, String v);
	
}