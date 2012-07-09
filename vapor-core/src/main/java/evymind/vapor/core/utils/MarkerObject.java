package evymind.vapor.core.utils;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MarkerObject implements Serializable {
	private String name;
	
	public MarkerObject(String name) {
		this.name=name;
	}
	@Override
    public String toString() {
		return name;
	}
}
