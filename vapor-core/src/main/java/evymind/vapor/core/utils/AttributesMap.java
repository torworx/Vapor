package evymind.vapor.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttributesMap implements Attributes {

	protected final Map<String, Object> map;

	/* ------------------------------------------------------------ */
	public AttributesMap() {
		this.map = new HashMap<String, Object>();
	}

	/* ------------------------------------------------------------ */
	public AttributesMap(Map<String, Object> map) {
		this.map = map;
	}

	/* ------------------------------------------------------------ */
	public AttributesMap(AttributesMap map) {
		this.map = new HashMap<String, Object>(map.map);
	}

	/* ------------------------------------------------------------ */
	/*
	 * @see org.eclipse.jetty.util.Attributes#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		this.map.remove(name);
	}

	/* ------------------------------------------------------------ */
	/*
	 * @see org.eclipse.jetty.util.Attributes#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object attribute) {
		if (attribute == null)
			this.map.remove(name);
		else
			this.map.put(name, attribute);
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String name) {
		return (T) this.map.get(name);
	}

	/* ------------------------------------------------------------ */
	/*
	 * @see org.eclipse.jetty.util.Attributes#getAttributeNames()
	 */
	public Enumeration<String> getAttributeNames() {
		return Collections.enumeration(this.map.keySet());
	}

	/* ------------------------------------------------------------ */
	/*
	 * @see org.eclipse.jetty.util.Attributes#getAttributeNames()
	 */
	public Set<String> getAttributeNameSet() {
		return this.map.keySet();
	}

	/* ------------------------------------------------------------ */
	public Set<Map.Entry<String, Object>> getAttributeEntrySet() {
		return this.map.entrySet();
	}

	/* ------------------------------------------------------------ */
	/*
	 * @see org.eclipse.jetty.util.Attributes#getAttributeNames()
	 */
	public static Enumeration<String> getAttributeNamesCopy(Attributes attrs) {
		if (attrs instanceof AttributesMap)
			return Collections.enumeration(((AttributesMap) attrs).map.keySet());

		List<String> names = new ArrayList<String>();
		names.addAll(Collections.list(attrs.getAttributeNames()));
		return Collections.enumeration(names);
	}

	/* ------------------------------------------------------------ */
	/*
	 * @see org.eclipse.jetty.util.Attributes#clear()
	 */
	public void clearAttributes() {
		this.map.clear();
	}

	/* ------------------------------------------------------------ */
	public int size() {
		return this.map.size();
	}

	/* ------------------------------------------------------------ */
	@Override
	public String toString() {
		return this.map.toString();
	}

	/* ------------------------------------------------------------ */
	public Set<String> keySet() {
		return this.map.keySet();
	}

	/* ------------------------------------------------------------ */
	public void addAll(Attributes attributes) {
		Enumeration<String> e = attributes.getAttributeNames();
		while (e.hasMoreElements()) {
			String name = e.nextElement();
			setAttribute(name, attributes.getAttribute(name));
		}
	}

}
