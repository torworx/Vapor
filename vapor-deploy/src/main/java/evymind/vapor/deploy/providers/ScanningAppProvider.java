package evymind.vapor.deploy.providers;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.io.Resource;
import evyframework.common.io.support.ResourcePatternResolver;
import evyframework.common.io.support.ResourcePatternUtils;
import evymind.vapor.core.utils.Scanner;
import evymind.vapor.core.utils.component.AbstractLifecycle;
import evymind.vapor.deploy.App;
import evymind.vapor.deploy.AppProvider;
import evymind.vapor.deploy.DeploymentManager;

public abstract class ScanningAppProvider extends AbstractLifecycle implements AppProvider {

	private static final Logger LOG = LoggerFactory.getLogger(ScanningAppProvider.class);

	protected ResourcePatternResolver resolver = ResourcePatternUtils.getFileAsDefaultResourcePatternResolver();
	
	private Map<String, App> _appMap = new HashMap<String, App>();

	private DeploymentManager _deploymentManager;
	protected final FilenameFilter _filenameFilter;
	private Resource _monitoredDir;
	private boolean _recursive = false;
	private int _scanInterval = 10;
	private Scanner _scanner;


	private final Scanner.DiscreteListener _scannerListener = new Scanner.DiscreteListener() {
		public void fileAdded(String filename) throws Exception {
			ScanningAppProvider.this.fileAdded(filename);
		}

		public void fileChanged(String filename) throws Exception {
			ScanningAppProvider.this.fileChanged(filename);
		}

		public void fileRemoved(String filename) throws Exception {
			ScanningAppProvider.this.fileRemoved(filename);
		}
	};


	protected ScanningAppProvider(FilenameFilter filter) {
		_filenameFilter = filter;
	}


	/**
	 * @return The index of currently deployed applications.
	 */
	protected Map<String, App> getDeployedApps() {
		return _appMap;
	}


	/**
	 * Called by the Scanner.DiscreteListener to create a new App object. Isolated in a method so that it is possible to
	 * override the default App object for specialized implementations of the AppProvider.
	 * 
	 * @param filename
	 *            The file that is the context.ecs.
	 * @return The App object for this particular context definition file.
	 */
	protected App createApp(String filename) {
		return new App(_deploymentManager, this, filename);
	}


	@Override
	protected void doStart() throws Exception {
		if (LOG.isDebugEnabled())
			LOG.debug(this.getClass().getSimpleName() + ".doStart()");
		if (_monitoredDir == null) {
			throw new IllegalStateException("No configuration dir specified");
		}

		File scandir = _monitoredDir.getFile();
		LOG.info("Deployment monitor " + scandir + " at interval " + _scanInterval);
		_scanner = new Scanner();
		_scanner.setScanDirs(Collections.singletonList(scandir));
		_scanner.setScanInterval(_scanInterval);
		_scanner.setRecursive(_recursive);
		_scanner.setFilenameFilter(_filenameFilter);
		_scanner.setReportDirs(true);
		_scanner.addListener(_scannerListener);
		_scanner.start();
	}


	@Override
	protected void doStop() throws Exception {
		if (_scanner != null) {
			_scanner.stop();
			_scanner.removeListener(_scannerListener);
			_scanner = null;
		}
	}


	protected void fileAdded(String filename) throws Exception {
		if (LOG.isDebugEnabled())
			LOG.debug("added {}", filename);
		App app = ScanningAppProvider.this.createApp(filename);
		if (app != null) {
			_appMap.put(filename, app);
			_deploymentManager.addApp(app);
		}
	}


	protected void fileChanged(String filename) throws Exception {
		if (LOG.isDebugEnabled())
			LOG.debug("changed {}", filename);
		App app = _appMap.remove(filename);
		if (app != null) {
			_deploymentManager.removeApp(app);
		}
		app = ScanningAppProvider.this.createApp(filename);
		if (app != null) {
			_appMap.put(filename, app);
			_deploymentManager.addApp(app);
		}
	}


	protected void fileRemoved(String filename) throws Exception {
		if (LOG.isDebugEnabled())
			LOG.debug("removed {}", filename);
		App app = _appMap.remove(filename);
		if (app != null)
			_deploymentManager.removeApp(app);
	}


	/**
	 * Get the deploymentManager.
	 * 
	 * @return the deploymentManager
	 */
	public DeploymentManager getDeploymentManager() {
		return _deploymentManager;
	}


	public Resource getMonitoredDirResource() {
		return _monitoredDir;
	}


	public String getMonitoredDirName() {
		return _monitoredDir.toString();
	}


	public int getScanInterval() {
		return _scanInterval;
	}


	public boolean isRecursive() {
		return _recursive;
	}


	public void setDeploymentManager(DeploymentManager deploymentManager) {
		_deploymentManager = deploymentManager;
	}


	public void setMonitoredDirResource(Resource contextsDir) {
		_monitoredDir = contextsDir;
	}


	public void addScannerListener(Scanner.Listener listener) {
		_scanner.addListener(listener);
	}


	/**
	 * @param location
	 *            Directory to scan for context descriptors or sar files
	 */
	public void setMonitoredDirName(String location) {
		try {
			setMonitoredDirResource(resolver.getResource(location));
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}


	protected void setRecursive(boolean recursive) {
		_recursive = recursive;
	}


	public void setScanInterval(int scanInterval) {
		_scanInterval = scanInterval;
	}
}
