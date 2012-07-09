package evymind.vapor.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import evymind.vapor.core.message.MessageType;


public interface Message {
	
    void initializeMessage(final Transport transport, String interfaceName, String messageName, MessageType messageType); 
    void initializeMessage(final Transport transport, String libraryName, String interfaceName, String messageName, MessageType messageType);

    void initializeRequestMessage(final Transport transport, String libraryName, String interfaceName, String messageName);
    void initializeResponseMessage(final Transport transport, String libraryName, String interfaceName, String messageName);
    void initializeEventMessage(final Transport transport, String libraryName, String interfaceName, String messageName);
    void initializeExceptionMessage(final Transport transport, String libraryName, String interfaceName, String messageName);

    void finalizeMessage();
    
    void releaseBuffer();
    
    void initializeRead(final Transport transport);
	
	void readFromBuffer(VaporBuffer buffer);
    void writeToBuffer(VaporBuffer buffer);
    void writeException(VaporBuffer buffer, Exception exception);
	

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

	<T> void write(String name, T object);
	
	<T> void write(String name, Object value, Class<T> type);
	
	void writeBigDecimal(String name, BigDecimal v);

	void writeBigInteger(String name, BigInteger v);

	void writeBoolean(String name, boolean v);

	void writeByinary(String name, byte[] b);
	
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
	
	UUID getClientId();
	
	void setClientId(UUID clientId);
	
	String getInterfaceName();
	
	void setInterfaceName(String interfaceName);
	
	String getMessageName();
	
	void setMessageName(String messageName);
	
	MessageType getMessageType();
	
	void setMessageType(MessageType messageType);
}
