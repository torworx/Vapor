package evymind.vapor.serializer.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Foo {
	
	private String stringValue;
	
	private int intValue;
	
	private float floatValue;
	
	private Date dateValue;
	
	private List<String> strings;
	
	public Foo() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Foo(String stringValue, int intValue, float floatValue) {
		super();
		this.stringValue = stringValue;
		this.intValue = intValue;
		this.floatValue = floatValue;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}

	public float getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(float floatValue) {
		this.floatValue = floatValue;
	}

	public Date getDateValue() {
		return dateValue;
	}

	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	public List<String> getStrings() {
		if (strings == null) {
			strings = new ArrayList<String>();
		}
		return strings;
	}

	public void setStrings(List<String> strings) {
		this.strings = strings;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dateValue == null) ? 0 : dateValue.hashCode());
		result = prime * result + Float.floatToIntBits(floatValue);
		result = prime * result + intValue;
		result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
		result = prime * result + ((strings == null) ? 0 : strings.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Foo other = (Foo) obj;
		if (dateValue == null) {
			if (other.dateValue != null)
				return false;
		} else if (!dateValue.equals(other.dateValue))
			return false;
		if (Float.floatToIntBits(floatValue) != Float.floatToIntBits(other.floatValue))
			return false;
		if (intValue != other.intValue)
			return false;
		if (stringValue == null) {
			if (other.stringValue != null)
				return false;
		} else if (!stringValue.equals(other.stringValue))
			return false;
		if (strings == null) {
			if (other.strings != null)
				return false;
		} else if (!strings.equals(other.strings))
			return false;
		return true;
	}


}
