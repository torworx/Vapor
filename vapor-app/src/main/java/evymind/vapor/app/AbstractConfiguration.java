package evymind.vapor.app;

public class AbstractConfiguration implements Configuration {

	@Override
	public void preConfigure(AppContext context) throws Exception {
	}

	@Override
	public void configure(AppContext context) throws Exception {
	}

	@Override
	public void postConfigure(AppContext context) throws Exception {
	}

	@Override
	public void deconfigure(AppContext context) throws Exception {
	}

	@Override
	public void destroy(AppContext context) throws Exception {
	}

	@Override
	public void cloneConfigure(AppContext template, AppContext context) throws Exception {
	}

}
