package evymind.vapor.core.utils;

import java.util.Enumeration;

/* ------------------------------------------------------------ */
/**
 * Attributes. Interface commonly used for storing attributes.
 * 
 */
public interface Attributes {
	
	public void removeAttribute(String name);

	public void setAttribute(String name, Object attribute);

	public <T> T getAttribute(String name);

	public Enumeration<String> getAttributeNames();

	public void clearAttributes();
}