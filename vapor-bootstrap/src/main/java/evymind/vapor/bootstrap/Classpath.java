package evymind.vapor.bootstrap;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Class to handle CLASSPATH construction
 */
public class Classpath {

	private final Vector<File> _elements = new Vector<File>();

	public Classpath() {
	}

	public Classpath(String initial) {
		addClasspath(initial);
	}

	public File[] getElements() {
		return _elements.toArray(new File[_elements.size()]);
	}

	public int count() {
		return _elements.size();
	}

	public boolean addComponent(String component) {
		if ((component != null) && (component.length() > 0)) {
			try {
				File f = new File(component);
				if (f.exists()) {
					File key = f.getCanonicalFile();
					if (!_elements.contains(key)) {
						_elements.add(key);
						return true;
					}
				}
			} catch (IOException e) {
			}
		}
		return false;
	}

	public boolean addComponent(File component) {
		if (component != null) {
			try {
				if (component.exists()) {
					File key = component.getCanonicalFile();
					if (!_elements.contains(key)) {
						_elements.add(key);
						return true;
					}
				}
			} catch (IOException e) {
			}
		}
		return false;
	}

	public boolean addClasspath(String s) {
		boolean added = false;
		if (s != null) {
			StringTokenizer t = new StringTokenizer(s, File.pathSeparator);
			while (t.hasMoreTokens()) {
				added |= addComponent(t.nextToken());
			}
		}
		return added;
	}

	public void dump(PrintStream out) {
		int i = 0;
		for (File element : _elements) {
			out.printf("%2d: %s\n", i++, element.getAbsolutePath());
		}
	}

	@Override
	public String toString() {
		StringBuffer cp = new StringBuffer(1024);
		int cnt = _elements.size();
		if (cnt >= 1) {
			cp.append(((_elements.elementAt(0))).getPath());
		}
		for (int i = 1; i < cnt; i++) {
			cp.append(File.pathSeparatorChar);
			cp.append(((_elements.elementAt(i))).getPath());
		}
		return cp.toString();
	}

	public ClassLoader getClassLoader() {
		int cnt = _elements.size();
		URL[] urls = new URL[cnt];
		for (int i = 0; i < cnt; i++) {
			try {
				urls[i] = _elements.elementAt(i).toURI().toURL();
			} catch (MalformedURLException e) {
			}
		}

		ClassLoader parent = Thread.currentThread().getContextClassLoader();
		if (parent == null) {
			parent = Classpath.class.getClassLoader();
		}
		if (parent == null) {
			parent = ClassLoader.getSystemClassLoader();
		}
		return new Loader(urls, parent);
	}

	private static class Loader extends URLClassLoader {
		Loader(URL[] urls, ClassLoader parent) {
			super(urls, parent);
		}

		@Override
		public String toString() {
			return "startJarLoader@" + Long.toHexString(hashCode());
		}
	}

	/**
	 * Overlay another classpath, copying its elements into place on this Classpath, while eliminating duplicate entries
	 * on the classpath.
	 * 
	 * @param cpOther
	 *            the other classpath to overlay
	 */
	public void overlay(Classpath cpOther) {
		for (File otherElement : cpOther._elements) {
			if (this._elements.contains(otherElement)) {
				// Skip duplicate entries
				continue;
			}
			this._elements.add(otherElement);
		}
	}

	public boolean isEmpty() {
		return (_elements == null) || (_elements.isEmpty());
	}
}
