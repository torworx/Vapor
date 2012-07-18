package evymind.vapor.app;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.PermissionCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.exception.MultiException;
import evyframework.common.io.Resource;
import evymind.vapor.core.utils.Loader;
import evymind.vapor.server.Connector;
import evymind.vapor.server.HandlerContainer;
import evymind.vapor.server.Server;
import evymind.vapor.server.handler.ContextHandler;
import evymind.vapor.server.session.SessionHandler;
import evymind.vapor.service.ServiceContextHandler;
import evymind.vapor.service.ServiceHandler;

public class AppContext extends ServiceContextHandler implements AppClassLoader.Context {

	private static final Logger log = LoggerFactory.getLogger(AppContext.class);

	public static final String TEMPDIR = "javax.service.context.tempdir";
	public static final String BASETEMPDIR = "evymind.vapor.app.basetempdir";
	public final static String WEB_DEFAULTS_XML = "evymind/vapor/app/appdefault.xml";
	public final static String ERROR_PAGE = "evymind.vapor.server.error_page";
	public final static String SERVER_CONFIG = "evymind.vapor.app.configuration";
	public final static String SERVER_SYS_CLASSES = "evymind.vapor.app.systemClasses";
	public final static String SERVER_SRV_CLASSES = "evymind.vapor.app.serverClasses";

	private static String[] DEFAULT_CONFIGURATION_CLASSES = { 
			"evymind.vapor.app.AppArchiveConfiguration",
			"evymind.vapor.app.AppConfigConfiguration" // configure app config file
	};

	// System classes are classes that cannot be replaced by
	// the web application, and they are *always* loaded via
	// system classloader.
	public final static String[] DEFAULT_SYSTEM_CLASSES = { 
			"java.", // Java SE classes
			"javax.", // Java SE classes
			"org.xml.", // needed by javax.xml
			"org.w3c.", // needed by javax.xml
			"org.slf4j.", // TODO: review if special case still needed
	};

	// Server classes are classes that are hidden from being
	// loaded by the service application using system classloader,
	// so if service application needs to load any of such classes,
	// it has to include them in its distribution.
	public final static String[] DEFAULT_SERVER_CLASSES = { 
			"-evymind.vapor.service.listener.", // don't hide useful
			"evymind.vapor." // hide other vapor classes
	};

	private String[] configurationClasses = DEFAULT_CONFIGURATION_CLASSES;
	private ClasspathPattern systemClasses = null;
	private ClasspathPattern serverClasses = null;

	private Configuration[] configurations;
	private String defaultsDescriptor = WEB_DEFAULTS_XML;
	private String descriptor = null;
	private final List<String> overrideDescriptors = new ArrayList<String>();
	private boolean distributable = false;
	private boolean extractSAR = true;
	private boolean copyDir = false;
	private boolean logUrlOnStart = false;
	private boolean parentLoaderPriority = Boolean.getBoolean("evymind.vapor.server.app.parentLoaderPriority");
	private PermissionCollection permissions;

	// private String[] _contextWhiteList = null;

	private File tmpDir;
	private String sar; // Vapor Application Archive
	private String extraClasspath;
	private Throwable unavailableException;

	private Map<String, String> resourceAliases;
	private boolean ownClassLoader = false;
	private boolean configurationDiscovered = true;
	private boolean configurationClassesSet = false;
	private boolean configurationsSet = false;
	private boolean allowDuplicateFragmentNames = false;
	private boolean throwUnavailableOnStartupException = false;

	private Metadata metadata = new Metadata();

	public static AppContext getCurrentAppContext() {
		ContextHandler.Context context = ContextHandler.getCurrentContext();
		if (context != null) {
			ContextHandler handler = context.getContextHandler();
			if (handler instanceof AppContext)
				return (AppContext) handler;
		}
		return null;
	}

	/* ------------------------------------------------------------ */
	public AppContext() {
		super(true);
		context = new Context();
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param contextPath
	 *            The context path
	 * @param app
	 *            The URL or filename of the app directory or sar file.
	 */
	public AppContext(String app, String contextPath) {
		super(null, contextPath, true);
		context = new Context();
		setContextPath(contextPath);
		setSar(app);
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param parent
	 *            The parent HandlerContainer.
	 * @param contextPath
	 *            The context path
	 * @param app
	 *            The URL or filename of the app directory or sar file.
	 */
	public AppContext(HandlerContainer parent, String app, String contextPath) {
		super(parent, contextPath, true);
		context = new Context();
		setSar(app);
	}

	/* ------------------------------------------------------------ */

	/**
	 * This constructor is used in the geronimo integration.
	 * 
	 * @param sessionHandler
	 *            SessionHandler for this web app
	 * @param serviceHandler
	 *            ServletHandler for this web app
	 */
	public AppContext(SessionHandler sessionHandler, ServiceHandler serviceHandler) {
		super(null, sessionHandler, serviceHandler);
		context = new Context();
	}

	public String getSar() {
		if (sar == null && getBaseResource() != null) {
			getBaseResource().toString();
		}
		return sar;
	}

	public void setSar(String sar) {
		this.sar = sar;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param serviceContextName
	 *            The serviceContextName to set.
	 */
	@Override
	public void setDisplayName(String serviceContextName) {
		super.setDisplayName(serviceContextName);
		ClassLoader cl = getClassLoader();
		if (cl != null && cl instanceof AppClassLoader && serviceContextName != null)
			((AppClassLoader) cl).setName(serviceContextName);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Get an exception that caused the app to be unavailable
	 * 
	 * @return A throwable if the app is unavailable or null
	 */
	public Throwable getUnavailableException() {
		return this.unavailableException;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Set Resource Alias. Resource aliases map resource uri's within a context. They may optionally be used by a
	 * handler when looking for a resource.
	 * 
	 * @param alias
	 * @param uri
	 */
	public void setResourceAlias(String alias, String uri) {
		if (this.resourceAliases == null)
			this.resourceAliases = new HashMap<String, String>(5);
		this.resourceAliases.put(alias, uri);
	}

	/* ------------------------------------------------------------ */
	public Map<String, String> getResourceAliases() {
		if (this.resourceAliases == null)
			return null;
		return this.resourceAliases;
	}

	/* ------------------------------------------------------------ */
	public void setResourceAliases(Map<String, String> map) {
		this.resourceAliases = map;
	}

	/* ------------------------------------------------------------ */
	public String getResourceAlias(String path) {
		if (this.resourceAliases == null)
			return null;
		String alias = this.resourceAliases.get(path);

		int slash = path.length();
		while (alias == null) {
			slash = path.lastIndexOf("/", slash - 1);
			if (slash < 0)
				break;
			String match = this.resourceAliases.get(path.substring(0, slash + 1));
			if (match != null)
				alias = match + path.substring(slash + 1);
		}
		return alias;
	}

	/* ------------------------------------------------------------ */
	public String removeResourceAlias(String alias) {
		if (this.resourceAliases == null)
			return null;
		return this.resourceAliases.remove(alias);
	}

	/* ------------------------------------------------------------ */
	/*
	 * (non-Javadoc)
	 * 
	 * @see evymind.vapor.server.handler.ContextHandler#setClassLoader(java.lang.ClassLoader)
	 */
	@Override
	public void setClassLoader(ClassLoader classLoader) {
		super.setClassLoader(classLoader);

		// if ( !(classLoader instanceof AppClassLoader) )
		// {
		// LOG.info("NOTE: detected a classloader which is not an instance of AppClassLoader being set on AppContext, some typical class and resource locations may be missing on: "
		// + toString() );
		// }

		if (classLoader != null && classLoader instanceof AppClassLoader && getDisplayName() != null)
			((AppClassLoader) classLoader).setName(getDisplayName());
	}

	/* ------------------------------------------------------------ */
	@Override
	public Resource getResource(String uriInContext) throws MalformedURLException {
		if (uriInContext == null || !uriInContext.startsWith("/"))
			throw new MalformedURLException(uriInContext);

		IOException ioe = null;
		Resource resource = null;
		int loop = 0;
		while (uriInContext != null && loop++ < 100) {
			try {
				resource = super.getResource(uriInContext);
				if (resource != null && resource.exists())
					return resource;

				uriInContext = getResourceAlias(uriInContext);
			} catch (IOException e) {
				if (ioe == null)
					ioe = e;
			}
		}

		if (ioe != null && ioe instanceof MalformedURLException)
			throw (MalformedURLException) ioe;

		return resource;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Is the context Automatically configured.
	 * 
	 * @return true if configuration discovery.
	 */
	public boolean isConfigurationDiscovered() {
		return this.configurationDiscovered;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Set the configuration discovery mode. If configuration discovery is set to true, then the JSR315 servlet 3.0
	 * discovered configuration features are enabled. These are:
	 * <ul>
	 * <li>Web Fragments</li>
	 * <li>META-INF/resource directories</li>
	 * </ul>
	 * 
	 * @param discovered
	 *            true if configuration discovery is enabled for automatic configuration from the context
	 */
	public void setConfigurationDiscovered(boolean discovered) {
		this.configurationDiscovered = discovered;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Pre configure the web application.
	 * <p>
	 * The method is normally called from {@link #start()}. It performs the discovery of the configurations to be
	 * applied to this context, specifically:
	 * <ul>
	 * <li>Instantiate the {@link Configuration} instances with a call to {@link #loadConfigurations()}.
	 * <li>Setup the default System classes by calling {@link #loadSystemClasses()}
	 * <li>Setup the default Server classes by calling <code>loadServerClasses()</code>
	 * <li>Instantiates a classload (if one is not already set)
	 * <li>Calls the {@link Configuration#preConfigure(AppContext)} method of all Configuration instances.
	 * </ul>
	 * 
	 * @throws Exception
	 */
	public void preConfigure() throws Exception {
		// Setup configurations
		loadConfigurations();

		// Setup system classes
		loadSystemClasses();

		// Setup server classes
		loadServerClasses();

		// Configure classloader
		this.ownClassLoader = false;
		if (getClassLoader() == null) {
			AppClassLoader classLoader = new AppClassLoader(this);
			setClassLoader(classLoader);
			this.ownClassLoader = true;
		}

		if (log.isDebugEnabled()) {
			ClassLoader loader = getClassLoader();
			log.debug("Thread Context classloader {}", loader);
			loader = loader.getParent();
			while (loader != null) {
				log.debug("Parent class loader: {} ", loader);
				loader = loader.getParent();
			}
		}

		// Prepare for configuration
		for (int i = 0; i < this.configurations.length; i++) {
			log.debug("preConfigure {} with {}", this, this.configurations[i]);
			this.configurations[i].preConfigure(this);
		}
	}

	/* ------------------------------------------------------------ */
	public void configure() throws Exception {
		// Configure app
		for (int i = 0; i < this.configurations.length; i++) {
			log.debug("configure {} with {}", this, this.configurations[i]);
			this.configurations[i].configure(this);
		}
	}

	/* ------------------------------------------------------------ */
	public void postConfigure() throws Exception {
		// Clean up after configuration
		for (int i = 0; i < this.configurations.length; i++) {
			log.debug("postConfigure {} with {}", this, this.configurations[i]);
			this.configurations[i].postConfigure(this);
		}
	}

	/* ------------------------------------------------------------ */
	/*
	 * @see org.eclipse.thread.AbstractLifeCycle#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		try {
			// _metadata.setAllowDuplicateFragmentNames(isAllowDuplicateFragmentNames());
			preConfigure();
			super.doStart();
			postConfigure();

			if (isLogUrlOnStart())
				dumpUrl();
		} catch (Exception e) {
			// start up of the app context failed, make sure it is not started
			log.warn("Failed startup of context " + this, e);
			this.unavailableException = e;
			setAvailable(false);
			if (isThrowUnavailableOnStartupException())
				throw e;
		}
	}

	/* ------------------------------------------------------------ */
	/*
	 * @see org.eclipse.thread.AbstractLifeCycle#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		super.doStop();

		try {
			for (int i = this.configurations.length; i-- > 0;)
				this.configurations[i].deconfigure(this);

			if (metadata != null)
				metadata.clear();
			metadata = new Metadata();

		} finally {
			if (this.ownClassLoader)
				setClassLoader(null);

			setAvailable(true);
			this.unavailableException = null;
		}
	}

	/* ------------------------------------------------------------ */
	@Override
	public void destroy() {
		// Prepare for configuration
		MultiException mx = new MultiException();
		if (this.configurations != null) {
			for (int i = this.configurations.length; i-- > 0;) {
				try {
					this.configurations[i].destroy(this);
				} catch (Exception e) {
					mx.add(e);
				}
			}
		}
		this.configurations = null;
		super.destroy();
		mx.ifExceptionThrowRuntime();
	}

	/* ------------------------------------------------------------ */
	/*
	 * Dumps the current web app name and URL to the log
	 */
	private void dumpUrl() {
		Connector[] connectors = getServer().getConnectors();
		for (int i = 0; i < connectors.length; i++) {
			String connectorName = connectors[i].getName();
			String displayName = getDisplayName();
			if (displayName == null)
				displayName = "App@" + connectors.hashCode();

			log.info(displayName + " at http://" + connectorName + getContextPath());
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return Returns the configurations.
	 */
	public String[] getConfigurationClasses() {
		return configurationClasses;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return Returns the configurations.
	 */
	public Configuration[] getConfigurations() {
		return this.configurations;
	}

	/* ------------------------------------------------------------ */
	/**
	 * The default descriptor is a web.xml format file that is applied to the context before the standard
	 * WEB-INF/web.xml
	 * 
	 * @return Returns the defaultsDescriptor.
	 */
	public String getDefaultsDescriptor() {
		return this.defaultsDescriptor;
	}

	/* ------------------------------------------------------------ */
	/**
	 * An override descriptor is a web.xml format file that is applied to the context after the standard WEB-INF/web.xml
	 * 
	 * @return Returns the Override Descriptor list
	 */
	public List<String> getOverrideDescriptors() {
		return Collections.unmodifiableList(this.overrideDescriptors);
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return Returns the permissions.
	 */
	public PermissionCollection getPermissions() {
		return this.permissions;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @see #setServerClasses(String[])
	 * @return Returns the serverClasses.
	 */
	public String[] getServerClasses() {
		if (this.serverClasses == null)
			loadServerClasses();

		return this.serverClasses.getPatterns();
	}

	public void addServerClass(String classname) {
		if (this.serverClasses == null)
			loadServerClasses();

		this.serverClasses.addPattern(classname);
	}

	/* ------------------------------------------------------------ */
	/**
	 * @see #setSystemClasses(String[])
	 * @return Returns the systemClasses.
	 */
	public String[] getSystemClasses() {
		if (this.systemClasses == null)
			loadSystemClasses();

		return this.systemClasses.getPatterns();
	}

	/* ------------------------------------------------------------ */
	public void addSystemClass(String classname) {
		if (this.systemClasses == null)
			loadSystemClasses();

		this.systemClasses.addPattern(classname);
	}

	/* ------------------------------------------------------------ */
	public boolean isServerClass(String name) {
		if (this.serverClasses == null)
			loadServerClasses();

		return this.serverClasses.match(name);
	}

	/* ------------------------------------------------------------ */
	public boolean isSystemClass(String name) {
		if (this.systemClasses == null)
			loadSystemClasses();

		return this.systemClasses.match(name);
	}

	/* ------------------------------------------------------------ */
	protected void loadSystemClasses() {
		if (this.systemClasses != null)
			return;

		// look for a Server attribute with the list of System classes
		// to apply to every web application. If not present, use our defaults.
		Server server = getServer();
		if (server != null) {
			Object systemClasses = server.getAttribute(SERVER_SYS_CLASSES);
			if (systemClasses != null && systemClasses instanceof String[])
				this.systemClasses = new ClasspathPattern((String[]) systemClasses);
		}

		if (this.systemClasses == null)
			this.systemClasses = new ClasspathPattern(DEFAULT_SYSTEM_CLASSES);
	}

	/* ------------------------------------------------------------ */
	private void loadServerClasses() {
		if (this.serverClasses != null) {
			return;
		}

		// look for a Server attribute with the list of Server classes
		// to apply to every web application. If not present, use our defaults.
		Server server = getServer();
		if (server != null) {
			Object serverClasses = server.getAttribute(SERVER_SRV_CLASSES);
			if (serverClasses != null && serverClasses instanceof String[]) {
				this.serverClasses = new ClasspathPattern((String[]) serverClasses);
			}
		}

		if (this.serverClasses == null) {
			this.serverClasses = new ClasspathPattern(DEFAULT_SERVER_CLASSES);
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return Returns the distributable.
	 */
	public boolean isDistributable() {
		return this.distributable;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return Returns the extractSAR.
	 */
	public boolean isExtractSAR() {
		return this.extractSAR;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return True if the webdir is copied (to allow hot replacement of jars on windows)
	 */
	public boolean isCopyDir() {
		return this.copyDir;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return True if the classloader should delegate first to the parent classloader (standard java behaviour) or
	 *         false if the classloader should first try to load from WEB-INF/lib or WEB-INF/classes (servlet spec
	 *         recommendation).
	 */
	public boolean isParentLoaderPriority() {
		return this.parentLoaderPriority;
	}

	/* ------------------------------------------------------------ */
	public String[] getDefaultConfigurationClasses() {
		return DEFAULT_CONFIGURATION_CLASSES;
	}

	/* ------------------------------------------------------------ */
	public String[] getDefaultServerClasses() {
		return DEFAULT_SERVER_CLASSES;
	}

	/* ------------------------------------------------------------ */
	public String[] getDefaultSystemClasses() {
		return DEFAULT_SYSTEM_CLASSES;
	}

	/* ------------------------------------------------------------ */
	protected void loadConfigurations() throws Exception {
		// if the configuration instances have been set explicitly, use them
		if (this.configurations != null)
			return;

		// if the configuration classnames have been set explicitly use them
		if (!this.configurationClassesSet)
			configurationClasses = DEFAULT_CONFIGURATION_CLASSES;

		this.configurations = new Configuration[configurationClasses.length];
		for (int i = 0; i < configurationClasses.length; i++) {
			this.configurations[i] = (Configuration) Loader.loadClass(this.getClass(), configurationClasses[i])
					.newInstance();
		}
	}

	/* ------------------------------------------------------------ */
	@Override
	public String toString() {
		return super.toString() + (sar == null ? "" : ("," + sar));
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param configurations
	 *            The configuration class names. If setConfigurations is not called these classes are used to create a
	 *            configurations array.
	 */
	public void setConfigurationClasses(String[] configurations) {
		if (isRunning())
			throw new IllegalStateException();
		configurationClasses = configurations == null ? null : (String[]) configurations.clone();
		this.configurationClassesSet = true;
		this.configurations = null;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param configurations
	 *            The configurations to set.
	 */
	public void setConfigurations(Configuration[] configurations) {
		if (isRunning())
			throw new IllegalStateException();
		this.configurations = configurations == null ? null : (Configuration[]) configurations.clone();
		this.configurationsSet = true;
	}

	/* ------------------------------------------------------------ */
	/**
	 * The default descriptor is a web.xml format file that is applied to the context before the standard
	 * WEB-INF/web.xml
	 * 
	 * @param defaultsDescriptor
	 *            The defaultsDescriptor to set.
	 */
	public void setDefaultsDescriptor(String defaultsDescriptor) {
		this.defaultsDescriptor = defaultsDescriptor;
	}

	/* ------------------------------------------------------------ */
	/**
	 * The override descriptor is a web.xml format file that is applied to the context after the standard
	 * WEB-INF/web.xml
	 * 
	 * @param overrideDescriptors
	 *            The overrideDescriptors (file or URL) to set.
	 */
	public void setOverrideDescriptors(List<String> overrideDescriptors) {
		this.overrideDescriptors.clear();
		this.overrideDescriptors.addAll(overrideDescriptors);
	}

	/* ------------------------------------------------------------ */
	/**
	 * The override descriptor is a web.xml format file that is applied to the context after the standard
	 * WEB-INF/web.xml
	 * 
	 * @param overrideDescriptor
	 *            The overrideDescriptor (file or URL) to add.
	 */
	public void addOverrideDescriptor(String overrideDescriptor) {
		this.overrideDescriptors.add(overrideDescriptor);
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return the web.xml descriptor to use. If set to null, WEB-INF/web.xml is used if it exists.
	 */
	public String getDescriptor() {
		return this.descriptor;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param descriptor
	 *            the web.xml descriptor to use. If set to null, WEB-INF/web.xml is used if it exists.
	 */
	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param distributable
	 *            The distributable to set.
	 */
	public void setDistributable(boolean distributable) {
		this.distributable = distributable;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param extractSAR
	 *            True if war files are extracted
	 */
	public void setExtractSAR(boolean extractSAR) {
		this.extractSAR = extractSAR;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param copy
	 *            True if the webdir is copied (to allow hot replacement of jars)
	 */
	public void setCopyWebDir(boolean copy) {
		this.copyDir = copy;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param java2compliant
	 *            The java2compliant to set.
	 */
	public void setParentLoaderPriority(boolean java2compliant) {
		this.parentLoaderPriority = java2compliant;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param permissions
	 *            The permissions to set.
	 */
	public void setPermissions(PermissionCollection permissions) {
		this.permissions = permissions;
	}

	// /**
	// * Set the context white list
	// *
	// * In certain circumstances you want may want to deny access of one app from another when you may not fully
	// trust
	// * the app. Setting this white list will enable a check when a servlet called getContext(String), validating
	// that
	// * the uriInPath for the given app has been declaratively allows access to the context.
	// *
	// * @param contextWhiteList
	// */
	// public void setContextWhiteList(String[] contextWhiteList) {
	// _contextWhiteList = contextWhiteList;
	// }

	/* ------------------------------------------------------------ */
	/**
	 * Set the server classes patterns.
	 * <p>
	 * Server classes/packages are classes used to implement the server and are hidden from the context. If the context
	 * needs to load these classes, it must have its own copy of them in WEB-INF/lib or WEB-INF/classes. A class pattern
	 * is a string of one of the forms:
	 * <dl>
	 * <dt>org.package.Classname</dt>
	 * <dd>Match a specific class</dd>
	 * <dt>org.package.</dt>
	 * <dd>Match a specific package hierarchy</dd>
	 * <dt>-org.package.Classname</dt>
	 * <dd>Exclude a specific class</dd>
	 * <dt>-org.package.</dt>
	 * <dd>Exclude a specific package hierarchy</dd>
	 * </dl>
	 * 
	 * @param serverClasses
	 *            The serverClasses to set.
	 */
	public void setServerClasses(String[] serverClasses) {
		this.serverClasses = new ClasspathPattern(serverClasses);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Set the system classes patterns.
	 * <p>
	 * System classes/packages are classes provided by the JVM and that cannot be replaced by classes of the same name
	 * from WEB-INF, regardless of the value of {@link #setParentLoaderPriority(boolean)}. A class pattern is a string
	 * of one of the forms:
	 * <dl>
	 * <dt>org.package.Classname</dt>
	 * <dd>Match a specific class</dd>
	 * <dt>org.package.</dt>
	 * <dd>Match a specific package hierarchy</dd>
	 * <dt>-org.package.Classname</dt>
	 * <dd>Exclude a specific class</dd>
	 * <dt>-org.package.</dt>
	 * <dd>Exclude a specific package hierarchy</dd>
	 * </dl>
	 * 
	 * @param systemClasses
	 *            The systemClasses to set.
	 */
	public void setSystemClasses(String[] systemClasses) {
		this.systemClasses = new ClasspathPattern(systemClasses);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Set temporary directory for context. The javax.servlet.context.tempdir attribute is also set.
	 * 
	 * @param dir
	 *            Writable temporary directory.
	 */
	public void setTempDirectory(File dir) {
		if (isStarted())
			throw new IllegalStateException("Started");

		if (dir != null) {
			try {
				dir = new File(dir.getCanonicalPath());
			} catch (IOException e) {
				log.warn(e.getMessage(), e);
			}
		}

		if (dir != null && !dir.exists()) {
			dir.mkdir();
			dir.deleteOnExit();
		}

		if (dir != null && (!dir.exists() || !dir.isDirectory() || !dir.canWrite()))
			throw new IllegalArgumentException("Bad temp directory: " + dir);

		try {
			if (dir != null)
				dir = dir.getCanonicalFile();
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		tmpDir = dir;
		setAttribute(TEMPDIR, tmpDir);
	}

	/* ------------------------------------------------------------ */
	public File getTempDirectory() {
		return tmpDir;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return Comma or semicolon separated path of filenames or URLs pointing to directories or jar files. Directories
	 *         should end with '/'.
	 */
	public String getExtraClasspath() {
		return this.extraClasspath;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @param extraClasspath
	 *            Comma or semicolon separated path of filenames or URLs pointing to directories or jar files.
	 *            Directories should end with '/'.
	 */
	public void setExtraClasspath(String extraClasspath) {
		this.extraClasspath = extraClasspath;
	}

	/* ------------------------------------------------------------ */
	public boolean isLogUrlOnStart() {
		return this.logUrlOnStart;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Sets whether or not the web app name and URL is logged on startup
	 * 
	 * @param logOnStart
	 *            whether or not the log message is created
	 */
	public void setLogUrlOnStart(boolean logOnStart) {
		this.logUrlOnStart = logOnStart;
	}

	/* ------------------------------------------------------------ */
	@Override
	public void setServer(Server server) {
		super.setServer(server);
		// if we haven't been given a set of configuration instances to
		// use, and we haven't been given a set of configuration classes
		// to use, use the configuration classes that came from the
		// Server (if there are any)
		if (!this.configurationsSet && !this.configurationClassesSet && server != null) {
			String[] serverConfigs = (String[]) server.getAttribute(SERVER_CONFIG);
			if (serverConfigs != null)
				setConfigurationClasses(serverConfigs);
		}
	}

	/* ------------------------------------------------------------ */
	public boolean isAllowDuplicateFragmentNames() {
		return this.allowDuplicateFragmentNames;
	}

	/* ------------------------------------------------------------ */
	public void setAllowDuplicateFragmentNames(boolean allowDuplicateFragmentNames) {
		this.allowDuplicateFragmentNames = allowDuplicateFragmentNames;
	}

	/* ------------------------------------------------------------ */
	public void setThrowUnavailableOnStartupException(boolean throwIfStartupException) {
		this.throwUnavailableOnStartupException = throwIfStartupException;
	}

	/* ------------------------------------------------------------ */
	public boolean isThrowUnavailableOnStartupException() {
		return this.throwUnavailableOnStartupException;
	}

	/* ------------------------------------------------------------ */
	@Override
	protected void startContext() throws Exception {
		configure();

		// resolve the metadata
		metadata.resolve(this);

		super.startContext();
	}

	/* ------------------------------------------------------------ */
	public class Context extends ServiceContextHandler.Context {
		/* ------------------------------------------------------------ */
		// @Override
		// public URL getResource(String path) throws MalformedURLException {
		// Resource resource = AppContext.this.getResource(path);
		// if (resource == null || !resource.exists())
		// return null;
		//
		// // Should we go to the original war?
		// if (resource.isDirectory() && resource instanceof ResourceCollection && !AppContext.this.isExtractSAR()) {
		// Resource[] resources = ((ResourceCollection) resource).getResources();
		// for (int i = resources.length; i-- > 0;) {
		// if (resources[i].getName().startsWith("jar:file"))
		// return resources[i].getURL();
		// }
		// }
		//
		// return resource.getURL();
		// }
		//
		// /* ------------------------------------------------------------ */
		// @Override
		// public ServletContext getContext(String uripath) {
		// ServletContext servletContext = super.getContext(uripath);
		//
		// if (servletContext != null && _contextWhiteList != null) {
		// for (String context : _contextWhiteList) {
		// if (context.equals(uripath)) {
		// return servletContext;
		// }
		// }
		//
		// return null;
		// } else {
		// return servletContext;
		// }
		// }

	}

	public Metadata getMetadata() {
		return this.metadata;
	}
}
