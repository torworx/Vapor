package evymind.vapor.config;

import java.util.Map;
import java.util.Properties;

import evyframework.common.io.Resource;

public interface Configuration {
	
	void setProperties(Properties properties);
	
	void setProperties(Map<String, String> properties);
	
	Object getInstance(String name) throws ConfigurationException;
	
	<T> T getInstance(String name, Class<T> requiredType) throws ConfigurationException;
	
	<T> T getInstance(Class<T> requiredType) throws ConfigurationException;
	
	boolean containsInstance(String name);
	
	<T> Map<String, T> getInstancesOfType(Class<T> type);
	
	void load(Resource... resources);
	
	void load(String... resourceLocations);
	
	void load(Class<?> relativeClass, String... resourceNames);
}
