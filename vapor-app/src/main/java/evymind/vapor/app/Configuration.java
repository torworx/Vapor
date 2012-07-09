package evymind.vapor.app;

/* ------------------------------------------------------------------------------- */
/**
 * Base Class for ApplicationContext Configuration. This class can be extended to customize or extend the
 * configuration of the ApplicationContext.
 */
public interface Configuration {

	/* ------------------------------------------------------------------------------- */
	/**
	 * Set up for configuration.
	 * <p>
	 * Typically this step discovers configuration resources
	 * 
	 * @param context
	 *            The context to configure
	 * @throws Exception
	 */
	public void preConfigure(AppContext context) throws Exception;

	/* ------------------------------------------------------------------------------- */
	/**
	 * Configure App.
	 * <p>
	 * Typically this step applies the discovered configuration resources to the {@link AppContext}
	 * 
	 * @param context
	 *            The context to configure
	 * @throws Exception
	 */
	public void configure(AppContext context) throws Exception;

	/* ------------------------------------------------------------------------------- */
	/**
	 * Clear down after configuration.
	 * 
	 * @param context
	 *            The context to configure
	 * @throws Exception
	 */
	public void postConfigure(AppContext context) throws Exception;

	/* ------------------------------------------------------------------------------- */
	/**
	 * DeConfigure App. This method is called to undo all configuration done. This is called to allow the context to
	 * work correctly over a stop/start cycle
	 * 
	 * @param context
	 *            The context to configure
	 * @throws Exception
	 */
	public void deconfigure(AppContext context) throws Exception;

	/* ------------------------------------------------------------------------------- */
	/**
	 * Destroy App. This method is called to destroy a appcontext. It is typically called when a context is
	 * removed from a server handler hierarchy by the deployer.
	 * 
	 * @param context
	 *            The context to configure
	 * @throws Exception
	 */
	public void destroy(AppContext context) throws Exception;

	/* ------------------------------------------------------------------------------- */
	/**
	 * Clone configuration instance.
	 * <p>
	 * Configure an instance of a AppContext, based on a template AppContext that has previously been configured
	 * by this Configuration.
	 * 
	 * @param template
	 *            The template context
	 * @param context
	 *            The context to configure
	 * @throws Exception
	 */
	public void cloneConfigure(AppContext template, AppContext context) throws Exception;
}
