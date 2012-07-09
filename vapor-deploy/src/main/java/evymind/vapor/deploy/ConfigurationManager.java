package evymind.vapor.deploy;

import java.util.Map;

/**
 * ConfigurationManager
 * 
 * Type for allow injection of property values for replacement in vapor xml files during deployment.
 */
public interface ConfigurationManager {
	
	public Map<String, String> getProperties();
}
