// ========================================================================
// Copyright (c) 2009-2009 Mort Bay Consulting Pty. Ltd.
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

package evymind.vapor.bootstrap;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import evymind.vapor.toolchain.test.TestingUtils;


/**
 */
public class MainTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		File testVaporHome = TestingUtils.getTestResourceDir("vapor.home");
		System.setProperty("vapor.home", testVaporHome.getAbsolutePath());
	}

	@Test
	public void testLoadBootstrapIni() throws IOException {
		Main main = new Main();
		List<String> args = main.parseBootstrapIniFiles();
		assertEquals("Expected 5 uncommented lines in bootstrap.ini", 9, args.size());
		assertEquals("First uncommented line in bootstrap.ini doesn't match expected result",
				"OPTIONS=Server,resources,ext", args.get(0));
		assertEquals("Last uncommented line in bootstrap.ini doesn't match expected result", "etc/vapor-testrealm.ecs",
				args.get(8));
	}

	@Test
	public void testExpandCommandLine() throws Exception {
		Main main = new Main();
		List<String> args = main.expandCommandLine(new String[] {});
		assertEquals("bootstrap.ini OPTIONS", "OPTIONS=Server,resources,ext", args.get(0));
		assertEquals("bootstrap.d/jmx OPTIONS", "OPTIONS=jmx", args.get(5));
		assertEquals("bootstrap.d/jmx Environment", "--pre=etc/vapor-jmx.ecs", args.get(6));
		assertEquals("bootstrap.d/websocket OPTIONS", "OPTIONS=websocket", args.get(7));
	}

	@Test
	public void testProcessCommandLine() throws Exception {
		Main main = new Main();
		List<String> args = main.expandCommandLine(new String[] {});
		List<String> xmls = main.processCommandLine(args);

		assertEquals("jmx --pre", "etc/vapor-jmx.ecs", xmls.get(0));
		assertEquals("bootstrap.ini", "etc/vapor.ecs", xmls.get(1));
		assertEquals("bootstrap.d", "etc/vapor-testrealm.ecs", xmls.get(5));
	}

	@Test
	public void testBuildCommandLine() throws IOException, NoSuchFieldException, IllegalAccessException {
		List<String> jvmArgs = new ArrayList<String>();
		jvmArgs.add("--exec");
		jvmArgs.add("-Xms1024m");
		jvmArgs.add("-Xmx1024m");

		List<String> configs = new ArrayList<String>();
		configs.add("vapor.ecs");
		configs.add("vapor-jmx.ecs");
		configs.add("vapor-logging.ecs");

		Main main = new Main();
		main.addJvmArgs(jvmArgs);

		Classpath classpath = nastyWayToCreateAClasspathObject("/vapor/home with spaces/");
		CommandLineBuilder cmd = main.buildCommandLine(classpath, configs);
		Assert.assertThat("CommandLineBuilder shouldn't be null", cmd, notNullValue());
		String commandLine = cmd.toString();
		Assert.assertThat("CommandLine shouldn't be null", commandLine, notNullValue());
		Assert.assertThat("Classpath should be correctly quoted and match expected value", commandLine,
				containsString("-cp /vapor/home with spaces/somejar.jar:/vapor/home with spaces/someotherjar.jar"));
		Assert.assertThat("CommandLine should contain jvmArgs", commandLine,
				containsString("--exec -Xms1024m -Xmx1024m"));
		Assert.assertThat("CommandLine should contain xmls", commandLine,
				containsString("vapor.ecs vapor-jmx.ecs vapor-logging.ecs"));

	}

	private Classpath nastyWayToCreateAClasspathObject(String vaporHome) throws NoSuchFieldException,
			IllegalAccessException {
		Classpath classpath = new Classpath();
		Field classpathElements = Classpath.class.getDeclaredField("_elements");
		classpathElements.setAccessible(true);
		File file = new File(vaporHome + "somejar.jar");
		File file2 = new File(vaporHome + "someotherjar.jar");
		Vector<File> elements = new Vector<File>();
		elements.add(file);
		elements.add(file2);
		classpathElements.set(classpath, elements);
		return classpath;
	}

}
