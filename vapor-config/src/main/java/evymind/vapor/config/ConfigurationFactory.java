package evymind.vapor.config;

public abstract class ConfigurationFactory {
	
	private ConfigurationFactory() {}
	
	public static Configuration createConfiguration() {
		return new DefaultConfiguration();
	}

}
