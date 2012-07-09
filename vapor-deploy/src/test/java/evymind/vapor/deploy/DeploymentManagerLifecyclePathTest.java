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

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.junit.Test;

import evymind.vapor.jmx.MBeanContainer;
import evymind.vapor.server.handler.ContextHandlerCollection;
import evymind.vapor.toolchain.jmx.JmxServiceConnection;

public class DeploymentManagerLifecyclePathTest {
	@Test
	public void testStateTransition_NewToDeployed() throws Exception {
		DeploymentManager depman = new DeploymentManager();
		depman.setDefaultLifecycleGoal(null); // no default
		AppLifecyclePathCollector pathtracker = new AppLifecyclePathCollector();
		MockAppProvider mockProvider = new MockAppProvider();

		depman.addLifecycleBinding(pathtracker);
		depman.addAppProvider(mockProvider);
		depman.setContexts(new ContextHandlerCollection());

		// Start DepMan
		depman.start();

		// Trigger new App
		mockProvider.findWebapp("foo-app-1.sar");

		App app = depman.getAppByOriginId("mock-foo-app-1.sar");

		// Request Deploy of App
		depman.requestAppGoal(app, "deployed");

		// Setup Expectations.
		List<String> expected = new ArrayList<String>();
		// SHOULD NOT SEE THIS NODE VISITED - expected.add("undeployed");
		expected.add("deploying");
		expected.add("deployed");

		pathtracker.assertExpected("Test StateTransition / New -> Deployed", expected);
	}

	@Test
	public void testStateTransition_Receive() throws Exception {
		DeploymentManager depman = new DeploymentManager();
		depman.setDefaultLifecycleGoal(null); // no default
		AppLifecyclePathCollector pathtracker = new AppLifecyclePathCollector();
		MockAppProvider mockProvider = new MockAppProvider();

		depman.addLifecycleBinding(pathtracker);
		depman.addAppProvider(mockProvider);

		// Start DepMan
		depman.start();

		// Trigger new App
		mockProvider.findWebapp("foo-app-1.sar");

		// Perform no goal request.

		// Setup Expectations.
		List<String> expected = new ArrayList<String>();

		pathtracker.assertExpected("Test StateTransition / New only", expected);
	}

	@Test
	public void testStateTransition_DeployedToUndeployed() throws Exception {
		DeploymentManager depman = new DeploymentManager();
		depman.setDefaultLifecycleGoal(null); // no default
		AppLifecyclePathCollector pathtracker = new AppLifecyclePathCollector();
		MockAppProvider mockProvider = new MockAppProvider();

		// Setup JMX
		MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
		mbContainer.start();
		mbContainer.addBean(depman);

		depman.addLifecycleBinding(pathtracker);
		depman.addAppProvider(mockProvider);
		depman.setContexts(new ContextHandlerCollection());

		// Start DepMan
		depman.start();

		// Trigger new App
		mockProvider.findWebapp("foo-app-1.sar");

		App app = depman.getAppByOriginId("mock-foo-app-1.sar");

		// Request Deploy of App
		depman.requestAppGoal(app, "deployed");

		JmxServiceConnection jmxConnection = new JmxServiceConnection();
		jmxConnection.connect();

		MBeanServerConnection mbsConnection = jmxConnection.getConnection();
		ObjectName dmObjName = new ObjectName("evymind.vapor.core.deploy:type=deploymentmanager,id=0");
		String[] params = new String[] { "mock-foo-app-1.sar", "undeployed" };
		String[] signature = new String[] { "java.lang.String", "java.lang.String" };
		mbsConnection.invoke(dmObjName, "requestAppGoal", params, signature);

		// Setup Expectations.
		List<String> expected = new ArrayList<String>();
		// SHOULD NOT SEE THIS NODE VISITED - expected.add("undeployed");
		expected.add("deploying");
		expected.add("deployed");
		expected.add("undeploying");
		expected.add("undeployed");

		pathtracker.assertExpected("Test JMX StateTransition / Deployed -> Undeployed", expected);
	}
}
