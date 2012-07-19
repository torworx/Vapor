package evymind.vapor.deploy;

import java.io.IOException;

import evymind.vapor.core.utils.component.Lifecycle;
import evymind.vapor.server.handler.ContextHandler;

/**
 * Object responsible for providing {@link App}s to the {@link DeploymentManager}
 */
public interface AppProvider extends Lifecycle {
	
	/**
	 * Set the Deployment Manager
	 * 
	 * @param deploymentManager
	 * @throws IllegalStateException
	 *             if the provider {@link #isRunning()}.
	 */
	void setDeploymentManager(DeploymentManager deploymentManager);


	/**
	 * Create a ContextHandler for an App
	 * 
	 * @param app
	 *            The App
	 * @return A ContextHandler
	 * @throws IOException
	 * @throws Exception
	 */
	ContextHandler createContextHandler(App app) throws Exception;
}