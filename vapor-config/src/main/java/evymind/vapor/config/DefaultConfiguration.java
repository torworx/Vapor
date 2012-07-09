package evymind.vapor.config;

import java.util.Map;
import java.util.Properties;

import evyframework.common.io.ClassPathResource;
import evyframework.common.io.Resource;
import evyframework.container.DefaultContainer;
import evyframework.container.factory.config.PropertyPlaceholderConfigurer;
import evyframework.container.script.ScriptFactoryBuilder;

public class DefaultConfiguration implements Configuration {
	
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

}
