package evymind.vapor.deploy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import evyframework.common.io.FileSystemResource;
import evyframework.common.io.Resource;

/**
 * FileConfigurationManager
 * 
 * Supplies properties defined in a file.
 */
public class FileConfigurationManager implements ConfigurationManager {
	
	private Resource file;
	private Map<String, String> map = new HashMap<String, String>();

	public FileConfigurationManager() {
	}

	public void setFile(String filename) throws MalformedURLException, IOException {
		this.file = new FileSystemResource(filename);
	}

	/**
	 * @see evymind.vapor.core.deploy.ConfigurationManager#getProperties()
	 */
	public Map<String, String> getProperties() {
		try {
			loadProperties();
			return this.map;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void loadProperties() throws FileNotFoundException, IOException {
		if (this.map.isEmpty() && this.file != null) {
			Properties properties = new Properties();
			properties.load(this.file.getInputStream());
			for (Map.Entry<Object, Object> entry : properties.entrySet())
				this.map.put(entry.getKey().toString(), String.valueOf(entry.getValue()));
		}
	}
}