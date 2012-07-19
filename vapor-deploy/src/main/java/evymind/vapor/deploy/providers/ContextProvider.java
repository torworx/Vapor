package evymind.vapor.deploy.providers;

import java.io.File;
import java.io.FilenameFilter;

import evyframework.common.io.Resource;
import evymind.vapor.config.Configuration;
import evymind.vapor.config.ConfigurationFactory;
import evymind.vapor.deploy.App;
import evymind.vapor.deploy.ConfigurationManager;
import evymind.vapor.deploy.utils.FileID;
import evymind.vapor.server.handler.ContextHandler;

/**
 * Context directory App Provider.
 * <p>
 * This specialization of {@link ScanningAppProvider} is the replacement for the old (and deprecated)
 * <code>evymind.vapor.deploy.ContextDeployer</code> and it will scan a directory only for context.xml files.
 */
public class ContextProvider extends ScanningAppProvider {
	
	private ConfigurationManager configurationManager;

	public ContextProvider() {
		super(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (!dir.exists())
					return false;
				String lowername = name.toLowerCase();
				if (lowername.startsWith("."))
					return false;

				return (lowername.endsWith(".xml") && !new File(dir, name).isDirectory());
			}
		});
	}


	public ConfigurationManager getConfigurationManager() {
		return this.configurationManager;
	}


	/**
	 * Set the configurationManager.
	 * 
	 * @param configurationManager
	 *            the configurationManager to set
	 */
	public void setConfigurationManager(ConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}


	public ContextHandler createContextHandler(App app) throws Exception {
		// Resource resource = resolver.getResource(app.getOriginId());
		// File file = resource.getFile();
		//
		// if (resource.exists() && FileID.isXmlFile(file)) {
		// XmlConfiguration xmlc = new XmlConfiguration(resource.getURL());
		//
		// xmlc.getIdMap().put("Server", getDeploymentManager().getServer());
		// if (getConfigurationManager() != null)
		// xmlc.getProperties().putAll(getConfigurationManager().getProperties());
		// return (ContextHandler) xmlc.configure();
		// }
		Resource resource = resolver.getResource(app.getOriginId());
		File file = resource.getFile();

		if (resource.exists() && FileID.isXmlFile(file)) {
			Configuration configuration = ConfigurationFactory.createConfiguration();
			configuration.setProperties(getConfigurationManager().getProperties());
			configuration.load(resource);
			return configuration.getInstance(ContextHandler.class);
		}

		throw new IllegalStateException("App resouce does not exist " + resource);
	}

}
