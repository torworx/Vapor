package evymind.vapor.client;

import evyframework.common.Assert;

public class Parameter {

	private String name;
	private Class<?> type;
	private Object value;

	public Parameter() {
	}

	public Parameter(String name, Object value) {
		this(name, null, value);
	}

	@SuppressWarnings("unchecked")
	public <T> Parameter(String name, Class<T> type, T value) {
//		Assert.hasText(name, "'name' must not be empty");
		Assert.isTrue(type != null || value != null, "'type' and 'value' can not are both null");
		if (type == null) {
			type = (Class<T>) value.getClass();
		}
		this.name = name;
		this.type = type;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Class<?> getType() {
		if (type == null) {
			return value == null ? null : value.getClass();
		}
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
