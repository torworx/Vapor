package evymind.vapor.config;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.io.ClassPathResource;
import evyframework.common.io.Resource;
import evyframework.common.io.support.ResourcePatternResolver;
import evyframework.common.io.support.ResourcePatternUtils;
import evyframework.container.DefaultContainer;
import evyframework.container.factory.config.PropertyPlaceholderConfigurer;
import evyframework.container.script.ScriptFactoryBuilder;
import evymind.vapor.core.utils.component.Lifecycle;
import evymind.vapor.core.utils.log.Logs;

public class DefaultConfiguration implements Configuration {

	private static final Logger log = LoggerFactory.getLogger(DefaultConfiguration.class);
	
	private static final ResourcePatternResolver RESOLVER = ResourcePatternUtils.getFileAsDefaultResourcePatternResolver();

	private final DefaultContainer container;
	private final PropertyPlaceholderConfigurer configurer;
	private final ScriptFactoryBuilder builder;

	public DefaultConfiguration() {
		this.container = new DefaultContainer();
		this.configurer = new PropertyPlaceholderConfigurer();
		this.configurer.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
		this.container.addBeanContextPostProcessor(this.configurer);
		this.builder = new ScriptFactoryBuilder(container);
	}

	@Override
	public void setProperties(Properties properties) {
		this.configurer.setProperties(properties);
	}

	@Override
	public void setProperties(Map<String, String> properties) {
		Properties p = new Properties();
		p.putAll(properties);
		setProperties(p);
	}

	@Override
	public Object getInstance(String name) throws ConfigurationException {
		return container.getInstance(name);
	}

	@Override
	public <T> T getInstance(String name, Class<T> requiredType) throws ConfigurationException {
		return container.getInstance(name, requiredType);
	}

	@Override
	public <T> T getInstance(Class<T> requiredType) throws ConfigurationException {
		return container.getInstance(requiredType);
	}

	@Override
	public boolean containsInstance(String name) {
		return container.getFactory(name) != null;
	}

	@Override
	public <T> Map<String, T> getInstancesOfType(Class<T> type) {
		return container.getInstancesOfType(type);
	}

	@Override
	public void load(Resource... resources) {
		builder.addFactories(resources);
	}

	@Override
	public void load(String... resourceLocations) {
		builder.addFactories(resourceLocations);
	}

	@Override
	public void load(Class<?> relativeClass, String... resourceNames) {
		Resource[] resources = new Resource[resourceNames.length];
		for (int i = 0; i < resourceNames.length; i++) {
			resources[i] = new ClassPathResource(resourceNames[i], relativeClass);
		}
		load(resources);
	}

	/**
	 * Run the configurations as a main application. The command line is used to
	 * obtain properties files (must be named '*.properties') and Configuration
	 * files.
	 * <p>
	 * Any property file on the command line is added to a combined Property
	 * instance that is passed to each configuration file via
	 * {@link DefaultConfiguration#setProperties(Map)}.
	 * <p>
	 * Each configuration file on the command line will to be loaded. If the
	 * object is an instance of {@link Lifecycle} in container, then it is
	 * started.
	 * <p>
	 * 
	 * @param args
	 *            array of property and configuration filenames or
	 *            {@link Resource}s.
	 */
	public static void main(final String[] args) throws Exception {

		final AtomicReference<Throwable> exception = new AtomicReference<Throwable>();

		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				try {

					Properties properties = null;

					// Look for properties from start.jar
					try {
						Class<?> config = DefaultConfiguration.class.getClassLoader().loadClass(
								"evymind.vapor.bootstrap.Config");
						properties = (Properties) config.getMethod("getProperties").invoke(null);
						log.debug("evymind.vapor.bootstrap.Config properties = {}", properties);
					} catch (NoClassDefFoundError e) {
						log.warn(Logs.IGNORED);
					} catch (ClassNotFoundException e) {
						log.warn(Logs.IGNORED);
					} catch (Exception e) {
						log.warn(e.getMessage(), e);
					}

					// If no start.config properties, use clean slate
					if (properties == null) {
						properties = new Properties();
						// Add System Properties
						Enumeration<?> ensysprop = System.getProperties().propertyNames();
						while (ensysprop.hasMoreElements()) {
							String name = (String) ensysprop.nextElement();
							properties.put(name, System.getProperty(name));
						}
					}

					// For all arguments, load properties or parse XMLs
					DefaultConfiguration configuration = new DefaultConfiguration();
					for (int i = 0; i < args.length; i++) {
						if (args[i].toLowerCase().endsWith(".properties")) {
							properties.load(RESOLVER.getResource(args[i]).getInputStream());
						} else {
							configuration.setProperties(properties);
							configuration.load(args[i]);
						}
					}

					// For all objects created by DefaultConfigurations, start them
					// if they are lifecycles.
					Map<String, Lifecycle>  lifecycles = configuration.getInstancesOfType(Lifecycle.class);
					for (Lifecycle lifecycle : lifecycles.values()) {
						if (!lifecycle.isRunning()) {
							lifecycle.start();
						}
					}
				} catch (Exception e) {
					log.debug(Logs.EXCEPTION, e);
					exception.set(e);
				}
				return null;
			}
		});

		Throwable th = exception.get();
		if (th != null) {
			if (th instanceof RuntimeException)
				throw (RuntimeException) th;
			else if (th instanceof Exception)
				throw (Exception) th;
			else if (th instanceof Error)
				throw (Error) th;
			throw new Error(th);
		}
	}
}
