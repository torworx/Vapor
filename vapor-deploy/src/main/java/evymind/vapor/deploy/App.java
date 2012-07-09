package evymind.vapor.deploy;

import evymind.vapor.server.handler.ContextHandler;

/**
 * The information about an App that is managed by the {@link DeploymentManager}
 */
public class App {
	private final DeploymentManager manager;
	private final AppProvider provider;
	private final String originId;
	private ContextHandler context;

	/**
	 * Create an App with specified Origin ID and archivePath
	 * 
	 * @param originId
	 *            the origin ID (The ID that the {@link AppProvider} knows about)
	 * @see App#getOriginId()
	 * @see App#getContextPath()
	 */
	public App(DeploymentManager manager, AppProvider provider, String originId) {
		this.manager = manager;
		this.provider = provider;
		this.originId = originId;
	}

	/**
	 * Create an App with specified Origin ID and archivePath
	 * 
	 * @param originId
	 *            the origin ID (The ID that the {@link AppProvider} knows about)
	 * @see App#getOriginId()
	 * @see App#getContextPath()
	 * @param context
	 *            Some implementations of AppProvider might have to use an already created ContextHandler.
	 */
	public App(DeploymentManager manager, AppProvider provider, String originId, ContextHandler context) {
		this(manager, provider, originId);
		this.context = context;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return The deployment manager
	 */
	public DeploymentManager getDeploymentManager() {
		return this.manager;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @return The AppProvider
	 */
	public AppProvider getAppProvider() {
		return this.provider;
	}

	/**
	 * Get ContextHandler for the App.
	 * 
	 * Create it if needed.
	 * 
	 * @return the {@link ContextHandler} to use for the App when fully started. (Portions of which might be ignored
	 *         when App is not yet {@link AppLifecycle#DEPLOYED} or {@link AppLifecycle#STARTED})
	 * @throws Exception
	 */
	public ContextHandler getContextHandler() throws Exception {
		if (this.context == null) {
			this.context = getAppProvider().createContextHandler(this);

//			AttributesMap attributes = this._manager.getContextAttributes();
//			if (attributes != null && attributes.size() > 0) {
//				// Merge the manager attributes under the existing attributes
//				attributes = new AttributesMap(attributes);
//				attributes.addAll(this._context.getAttributes());
//				this._context.setAttributes(attributes);
//			}
		}
		return this.context;
	}

	/**
	 * The context path {@link App} relating to how it is installed on the vapor server side.
	 * 
	 * @return the contextPath for the App
	 */
	public String getContextPath() {
		if (this.context == null) {
			return null;
		}
		return this.context.getContextPath();
	}

	/**
	 * The origin of this {@link App} as specified by the {@link AppProvider}
	 * 
	 * @return String representing the origin of this app.
	 */
	public String getOriginId() {
		return this.originId;
	}

	@Override
	public String toString() {
		return "App[" + this.context + "," + this.originId + "]";
	}
}
