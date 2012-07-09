package evymind.vapor.core.serializer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

import evyframework.common.Assert;
import evyframework.common.ClassUtils;

public abstract class AbstractSerializer implements Serializer {
	
	/*
	protected abstract <T> T beginReadObject(String name, Class<T> type);
	protected abstract <T> void endReadObject(String name, T object);
	protected abstract <T> void beginWriteObject(String name, T object);
	protected abstract <T> void endWriteObject(String name, T object);
	
	protected abstract <T> void doReadObject(String name, T object);
	protected abstract <T> void doWriteObject(String name, T object);
	
	
	public <T> T readObject(String name, Class<T> clazz) {
		T answer = beginReadObject(name, clazz);
		if (answer == null) {
			return null;
		}
		doReadObject(name, answer);
		endReadObject(name, answer);
		return answer;
	}

	@Override
	public <T> void readObject(String name, T object) {
		doReadObject(name, object);
	}
	
	public <T> void writeObject(String name, T object) {
		beginWriteObject(name, object);
		if (object != null) {
			doWriteObject(name, object);
			endWriteObject(name, object);
		}
	}
	*/
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T read(String name, Class<T> clazz) {
		Object answer = null;
		if (ClassUtils.isAssignable(Boolean.class, clazz)) {
			answer = readBoolean(name);
		} else if (ClassUtils.isAssignable(Byte.class, clazz)) {
			answer = readByte(name);
		} else if (ClassUtils.isAssignable(Character.class, clazz)) {
			answer = readChar(name);
		} else if (ClassUtils.isAssignable(Double.class, clazz)) {
			answer = readDouble(name);
		} else if (ClassUtils.isAssignable(Float.class, clazz)) {
			answer = readFloat(name);
		} else if (ClassUtils.isAssignable(Integer.class, clazz)) {
			answer = readInteger(name);
		} else if (ClassUtils.isAssignable(Long.class, clazz)) {
			answer = readLong(name);
		} else if (ClassUtils.isAssignable(Short.class, clazz)) {
			answer = readShort(name);
		} else if (String.class.equals(clazz)) { // TODO: how to support UTF string?
			answer = readString(name);
		} else if (BigDecimal.class.equals(clazz)) {
			answer = readBigDecimal(name);
		} else if (BigInteger.class.equals(clazz)) {
			answer = readBigInteger(name);
		} else if (Date.class.equals(clazz)) {
			answer = readDate(name);
		} else if (Timestamp.class.equals(clazz)) {
			answer = readTimestamp(name);
		} else {
			answer = readObject(name);
		}
		
		return (T) answer;
	}
	
	@Override
	public <T> void write(String name, T value) {
		if (value != null) {
			write(name, value, value.getClass());
		} else {
			writeObject(name, value);
		}
	}

	@Override
	public <T> void write(String name, Object value, Class<T> type) {
		Assert.notNull(value, "'value' must not be null");
		Assert.notNull(type, "'type' must not be null");
		if (ClassUtils.isAssignable(Boolean.class, type)) {
			writeBoolean(name, (Boolean) value);
		} else if (ClassUtils.isAssignable(Byte.class, type)) {
			writeByte(name, (Byte) value);
		} else if (ClassUtils.isAssignable(Character.class, type)) {
			writeChar(name, (Character) value);
		} else if (ClassUtils.isAssignable(Double.class, type)) {
			writeDouble(name, (Double) value);
		} else if (ClassUtils.isAssignable(Float.class, type)) {
			writeFloat(name, (Float) value);
		} else if (ClassUtils.isAssignable(Integer.class, type)) {
			writeInteger(name, (Integer) value);
		} else if (ClassUtils.isAssignable(Long.class, type)) {
			writeLong(name, (Long) value);
		} else if (ClassUtils.isAssignable(Short.class, type)) {
			writeShort(name, (Short) value);
		} else if (String.class.equals(type)) { // TODO: how to support UTF string?
			writeString(name, (String) value);
		} else if (BigDecimal.class.equals(type)) {
			writeBigDecimal(name, (BigDecimal) value);
		} else if (BigInteger.class.equals(type)) {
			writeBigInteger(name, (BigInteger) value);
		} else if (Date.class.equals(type)) {
			writeDate(name, (Date) value);
		} else if (Timestamp.class.equals(type)) {
			writeTimestamp(name, (Timestamp) value);
		} else {
			writeObject(name, value);
		}
	}
	
}
