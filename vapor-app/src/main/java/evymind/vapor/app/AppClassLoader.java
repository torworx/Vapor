package evymind.vapor.app;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.io.Resource;
import evyframework.common.io.support.ResourcePatternResolver;
import evyframework.common.io.support.ResourcePatternUtils;

/* ------------------------------------------------------------ */
/**
 * ClassLoader for Context. Specializes URLClassLoader with some utility and file mapping methods.
 * 
 * This loader defaults to the 2.3 servlet spec behavior where non system classes are loaded from the classpath in
 * preference to the parent loader. Java2 compliant loading, where the parent loader always has priority, can be
 * selected with the {@link evymind.vapor.app.AppContext#setParentLoaderPriority(boolean)} method and
 * influenced with {@link AppContext#isServerClass(String)} and {@link AppContext#isSystemClass(String)}.
 * 
 * If no parent class loader is provided, then the current thread context classloader will be used. If that is null then
 * the classloader that loaded this class is used as the parent.
 * 
 */
public class AppClassLoader extends URLClassLoader {

	private static final Logger log = LoggerFactory.getLogger(AppClassLoader.class);

	private ResourcePatternResolver resolver = ResourcePatternUtils.getFileAsDefaultResourcePatternResolver();

	private final Context context;
	private final ClassLoader parent;
	private final Set<String> extensions = new HashSet<String>();
	private String name = String.valueOf(hashCode());

	/* ------------------------------------------------------------ */
	/**
	 * The Context in which the classloader operates.
	 */
	public interface Context {

		Resource newResource(String location) throws IOException;

		// Resource[] getResources(String locationPattern) throws IOException;

		/* ------------------------------------------------------------ */
		/**
		 * @return Returns the permissions.
		 */
		PermissionCollection getPermissions();

		/* ------------------------------------------------------------ */
		/**
		 * Is the class a System Class. A System class is a class that is visible to a svcapplication, but that cannot
		 * be overridden by the contents of WEB-INF/lib or WEB-INF/classes
		 * 
		 * @param clazz
		 *            The fully qualified name of the class.
		 * @return True if the class is a system class.
		 */
		boolean isSystemClass(String clazz);

		/* ------------------------------------------------------------ */
		/**
		 * Is the class a Server Class. A Server class is a class that is part of the implementation of the server and
		 * is NIT visible to a svcapplication. The web application may provide it's own implementation of the class, to
		 * be loaded from WEB-INF/lib or WEB-INF/classes
		 * 
		 * @param clazz
		 *            The fully qualified name of the class.
		 * @return True if the class is a server class.
		 */
		boolean isServerClass(String clazz);

		/* ------------------------------------------------------------ */
		/**
		 * @return True if the classloader should delegate first to the parent classloader (standard java behaviour) or
		 *         false if the classloader should first try to load from WEB-INF/lib or WEB-INF/classes (servlet spec
		 *         recommendation).
		 */
		boolean isParentLoaderPriority();

		/* ------------------------------------------------------------ */
		String getExtraClasspath();

	}

	/* ------------------------------------------------------------ */
	/**
	 * Constructor.
	 */
	public AppClassLoader(Context context) throws IOException {
		this(null, context);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Constructor.
	 */
	public AppClassLoader(ClassLoader parent, Context context) throws IOException {
		super(new URL[] {}, parent != null ? parent : (Thread.currentThread().getContextClassLoader() != null ? Thread
				.currentThread().getContextClassLoader()
				: (AppClassLoader.class.getClassLoader() != null ? AppClassLoader.class.getClassLoader()
						: ClassLoader.getSystemClassLoader())));
		this.parent = getParent();
		this.context = context;
		if (this.parent == null)
			throw new IllegalArgumentException("no parent classloader!");

		this.extensions.add(".jar");
		this.extensions.add(".sar");
		this.extensions.add(".zip");

		// TODO remove this system property
		String extensions = System.getProperty(AppClassLoader.class.getName() + ".extensions");
		if (extensions != null) {
			StringTokenizer tokenizer = new StringTokenizer(extensions, ",;");
			while (tokenizer.hasMoreTokens())
				this.extensions.add(tokenizer.nextToken().trim());
		}

		if (context.getExtraClasspath() != null)
			addClassPath(context.getExtraClasspath());
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return the name of the classloader
	 */
	public String getName() {
		return this.name;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param name
	 *            the name of the classloader
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* ------------------------------------------------------------ */
	public Context getContext() {
		return context;
	}

	public void addClassPath(Resource... resources) throws IOException {
		for (Resource r : resources) {
			addURL(r.getURL());
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param classPath
	 *            Comma or semicolon separated path of filenames or URLs pointing to directories or jar files.
	 *            Directories should end with '/'.
	 */
	public void addClassPath(String locationPattern) throws IOException {
		addClassPath(context.newResource(locationPattern));
	}

	private String[] forJars(Resource lib) throws IOException {
		String[] answer = new String[this.extensions.size()];
		String basepath = lib.getFile().getAbsolutePath() + "/*";
		int i = 0;
		for (String ext : this.extensions) {
			answer[i++] = basepath + ext;
		}
		return answer;

	}

	/* ------------------------------------------------------------ */
	/**
	 * Add elements to the class path for the context from the jar and zip files found in the specified resource.
	 * 
	 * @param lib
	 *            the resource that contains the jar and/or zip files.
	 */
	public void addJars(Resource lib) {
		try {
			if (lib.exists() && lib.getFile().isDirectory()) {
				String[] jarPathPatterns = forJars(lib);
				for (String jarPathPattern : jarPathPatterns) {
					addClassPath(resolver.getResources(jarPathPattern));
				}
			}
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		
	}

	/* ------------------------------------------------------------ */
	public PermissionCollection getPermissions(CodeSource cs) {
		// TODO check CodeSource
		PermissionCollection permissions = context.getPermissions();
		PermissionCollection pc = (permissions == null) ? super.getPermissions(cs) : permissions;
		return pc;
	}

	/* ------------------------------------------------------------ */
	public Enumeration<URL> getResources(String name) throws IOException {
		boolean system_class = context.isSystemClass(name);
		boolean server_class = context.isServerClass(name);

		List<URL> from_parent = toList(server_class ? null : parent.getResources(name));
		List<URL> from_svcapp = toList((system_class && !from_parent.isEmpty()) ? null : this.findResources(name));

		if (context.isParentLoaderPriority()) {
			from_parent.addAll(from_svcapp);
			return Collections.enumeration(from_parent);
		}
		from_svcapp.addAll(from_parent);
		return Collections.enumeration(from_svcapp);
	}

	/* ------------------------------------------------------------ */
	private List<URL> toList(Enumeration<URL> e) {
		if (e == null)
			return new ArrayList<URL>();
		return Collections.list(e);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Get a resource from the classloader
	 * 
	 * NOTE: this method provides a convenience of hacking off a leading / should one be present. This is non-standard
	 * and it is recommended to not rely on this behavior
	 */
	public URL getResource(String name) {
		URL url = null;
		boolean tried_parent = false;
		boolean system_class = context.isSystemClass(name);
		boolean server_class = context.isServerClass(name);

		if (system_class && server_class)
			return null;

		if (parent != null && (context.isParentLoaderPriority() || system_class) && !server_class) {
			tried_parent = true;

			if (parent != null)
				url = parent.getResource(name);
		}

		if (url == null) {
			url = this.findResource(name);

			if (url == null && name.startsWith("/")) {
				if (log.isDebugEnabled())
					log.debug("HACK leading / off " + name);
				url = this.findResource(name.substring(1));
			}
		}

		if (url == null && !tried_parent && !server_class) {
			if (parent != null)
				url = parent.getResource(name);
		}

		if (url != null)
			if (log.isDebugEnabled())
				log.debug("getResource(" + name + ")=" + url);

		return url;
	}

	/* ------------------------------------------------------------ */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return loadClass(name, false);
	}

	/* ------------------------------------------------------------ */
	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		Class<?> c = findLoadedClass(name);
		ClassNotFoundException ex = null;
		boolean tried_parent = false;

		boolean system_class = context.isSystemClass(name);
		boolean server_class = context.isServerClass(name);

		if (system_class && server_class) {
			return null;
		}

		if (c == null && parent != null && (context.isParentLoaderPriority() || system_class) && !server_class) {
			tried_parent = true;
			try {
				c = parent.loadClass(name);
				if (log.isDebugEnabled())
					log.debug("loaded " + c);
			} catch (ClassNotFoundException e) {
				ex = e;
			}
		}

		if (c == null) {
			try {
				c = this.findClass(name);
			} catch (ClassNotFoundException e) {
				ex = e;
			}
		}

		if (c == null && parent != null && !tried_parent && !server_class)
			c = parent.loadClass(name);

		if (c == null)
			throw ex;

		if (resolve)
			resolveClass(c);

		if (log.isDebugEnabled())
			log.debug("loaded " + c + " from " + c.getClassLoader());

		return c;
	}

	/* ------------------------------------------------------------ */
	public String toString() {
		return "AppClassLoader=" + this.name + "@" + Long.toHexString(hashCode());
	}
}