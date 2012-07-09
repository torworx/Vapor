// ========================================================================
// Copyright (c) 2010 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses.
// ========================================================================
package evymind.vapor.app;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import evymind.vapor.app.Configuration;
import evymind.vapor.app.AppArchiveConfiguration;
import evymind.vapor.app.AppContext;
import evymind.vapor.server.Server;

public class AppContextTest {
	
	@Test
	public void testConfigurationClassesFromDefault() {
		Server server = new Server();
		// test if no classnames set, its the defaults
		AppContext wac = new AppContext();
		assertNull(wac.getConfigurations());
		String[] classNames = wac.getConfigurationClasses();
		assertNotNull(classNames);

		// test if no classname set, and none from server its the defaults
		wac.setServer(server);
		assertTrue(Arrays.equals(classNames, wac.getConfigurationClasses()));
	}

	@Test
	public void testConfigurationClassesExplicit() {
		String[] classNames = { "x.y.z" };

		Server server = new Server();
		server.setAttribute(AppContext.SERVER_CONFIG, classNames);

		// test an explicitly set classnames list overrides that from the server
		AppContext wac = new AppContext();
		String[] myClassNames = { "a.b.c", "d.e.f" };
		wac.setConfigurationClasses(myClassNames);
		wac.setServer(server);
		String[] names = wac.getConfigurationClasses();
		assertTrue(Arrays.equals(myClassNames, names));

		// test if no explicit classnames, they come from the server
		AppContext wac2 = new AppContext();
		wac2.setServer(server);
		assertTrue(Arrays.equals(classNames, wac2.getConfigurationClasses()));
	}

	@Test
	public void testConfigurationInstances() {
		Configuration[] configs = { new AppArchiveConfiguration() };
		AppContext wac = new AppContext();
		wac.setConfigurations(configs);
		assertTrue(Arrays.equals(configs, wac.getConfigurations()));

		// test that explicit config instances override any from server
		String[] classNames = { "x.y.z" };
		Server server = new Server();
		server.setAttribute(AppContext.SERVER_CONFIG, classNames);
		wac.setServer(server);
		assertTrue(Arrays.equals(configs, wac.getConfigurations()));
	}

//	@Test
//	public void testRealPathDoesNotExist() throws Exception {
//		Server server = new Server();
//		AppContext context = new AppContext(".", "/");
//		server.setHandler(context);
//		server.start();
//
//		// When
//		ServiceContext ctx = context.getServiceContext();
//
//		// Then
//		// This passes:
//		assertNotNull(ctx.getRealPath("/doesnotexist"));
//		// This fails:
//		assertNotNull(ctx.getRealPath("/doesnotexist/"));
//	}
//
//	/**
//	 * tests that the servlet context white list works
//	 * 
//	 * @throws Exception
//	 */
//	@Test
//	public void testContextWhiteList() throws Exception {
//		Server server = new Server();
//		HandlerList handlers = new HandlerList();
//		AppContext contextA = new AppContext(".", "/A");
//
//		contextA.addService(ServiceA.class, "/s");
//		handlers.addHandler(contextA);
//		AppContext contextB = new AppContext(".", "/B");
//
//		contextB.addService(ServiceB.class, "/s");
//		contextB.setContextWhiteList(new String[] { "/doesnotexist", "/B/s" });
//		handlers.addHandler(contextB);
//
//		server.setHandler(handlers);
//		server.start();
//
//		// context A should be able to get both A and B servlet contexts
//		Assert.assertNotNull(contextA.getServiceHandler().getServiceContext().getContext("/A/s"));
//		Assert.assertNotNull(contextA.getServiceHandler().getServiceContext().getContext("/B/s"));
//
//		// context B has a contextWhiteList set and should only be able to get ones that are approved
//		Assert.assertNull(contextB.getServiceHandler().getServiceContext().getContext("/A/s"));
//		Assert.assertNotNull(contextB.getServiceHandler().getServiceContext().getContext("/B/s"));
//	}
//
//	@Test
//	public void testAlias() throws Exception {
//		File dir = File.createTempFile("dir", null);
//		dir.delete();
//		dir.mkdir();
//		dir.deleteOnExit();
//
//		File webinf = new File(dir, "WEB-INF");
//		webinf.mkdir();
//
//		File classes = new File(dir, "classes");
//		classes.mkdir();
//
//		File someclass = new File(classes, "SomeClass.class");
//		someclass.createNewFile();
//
//		AppContext context = new AppContext();
//		context.setBaseResource(new ResourceCollection(dir.getAbsolutePath()));
//
//		context.setResourceAlias("/WEB-INF/classes/", "/classes/");
//
//		assertTrue(Resource.newResource(context.getServiceContext().getResource("/WEB-INF/classes/SomeClass.class"))
//				.exists());
//		assertTrue(Resource.newResource(context.getServiceContext().getResource("/classes/SomeClass.class")).exists());
//
//	}
//
//	@Test
//	public void testIsProtected() throws Exception {
//		AppContext context = new AppContext();
//		assertTrue(context.isProtectedTarget("/web-inf/lib/foo.jar"));
//		assertTrue(context.isProtectedTarget("/meta-inf/readme.txt"));
//		assertFalse(context.isProtectedTarget("/something-else/web-inf"));
//	}
//
//	class ServiceA extends GenericService {
//		@Override
//		public void service(ServiceRequest req, ServiceResponse res) throws ServiceException, IOException {
//			this.getServiceContext().getContext("/A/s");
//		}
//	}
//
//	class ServiceB extends GenericService {
//		@Override
//		public void service(ServiceRequest req, ServiceResponse res) throws ServiceException, IOException {
//			this.getServiceContext().getContext("/B/s");
//		}
//	}
}
