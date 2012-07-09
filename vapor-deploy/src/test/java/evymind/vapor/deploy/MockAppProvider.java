// ========================================================================
// Copyright (c) Webtide LLC
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
//
// The Eclipse Public License is available at 
// http://www.eclipse.org/legal/epl-v10.html
//
// The Apache License v2.0 is available at
// http://www.apache.org/licenses/LICENSE-2.0.txt
//
// You may elect to redistribute this code under either of these licenses. 
// ========================================================================
package evymind.vapor.deploy;

import java.io.File;

import evymind.vapor.app.AppContext;
import evymind.vapor.core.utils.component.AbstractLifecycle;
import evymind.vapor.deploy.utils.FileID;
import evymind.vapor.server.handler.ContextHandler;
import evymind.vapor.toolchain.test.TestingUtils;

public class MockAppProvider extends AbstractLifecycle implements AppProvider {
	private DeploymentManager deployMan;
	private File webappsDir;

	public void setDeploymentManager(DeploymentManager deploymentManager) {
		this.deployMan = deploymentManager;
	}

	@Override
	public void doStart() {
		this.webappsDir = TestingUtils.getTestResourceDir("apps");
	}

	public void findWebapp(String name) {
		App app = new App(deployMan, this, "mock-" + name);
		this.deployMan.addApp(app);
	}

	public ContextHandler createContextHandler(App app) throws Exception {
		AppContext context = new AppContext();

		File sar = new File(webappsDir, app.getOriginId().substring(5));
		context.setSar(sar.toString());

		String path = sar.getName();

		if (FileID.isAppArchiveFile(sar)) {
			// Context Path is the same as the archive.
			path = path.substring(0, path.length() - 4);
		}

		// special case of archive (or dir) named "root" is / context
		if (path.equalsIgnoreCase("root") || path.equalsIgnoreCase("root/"))
			path = "/";

		// Ensure "/" is Prepended to all context paths.
		if (path.charAt(0) != '/')
			path = "/" + path;

		// Ensure "/" is Not Trailing in context paths.
		if (path.endsWith("/") && path.length() > 0)
			path = path.substring(0, path.length() - 1);

		context.setContextPath(path);

		return context;
	}
}
