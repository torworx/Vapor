package evymind.vapor.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import evyframework.common.io.Resource;
import evyframework.common.io.support.ResourcePatternResolver;
import evyframework.common.io.support.ResourcePatternUtils;
import evymind.vapor.app.AppClassLoader;
import evymind.vapor.app.AppContext;

public class AppClassLoaderTest {
	
	private ResourcePatternResolver resolver = ResourcePatternUtils.getFileAsDefaultResourcePatternResolver();
	
	private AppContext context;
	private AppClassLoader loader;

	@Before
	public void init() throws Exception {
		Resource appres = resolver.getResource("./src/test/svcapp/");
		
		assertTrue(appres.exists());

		context = new AppContext();
		context.setBaseResource(appres);
		context.setContextPath("/test");

		loader = new AppClassLoader(context);
		loader.addJars(appres.createRelative("lib"));
		loader.addClassPath(appres.createRelative("classes"));
		loader.setName("test");
	}

	@Test
	public void testParentLoad() throws Exception {
		context.setParentLoaderPriority(true);
		assertTrue(canLoadClass("org.acme.webapp.ClassInJarA"));
		assertTrue(canLoadClass("org.acme.webapp.ClassInJarB"));
		assertTrue(canLoadClass("org.acme.other.ClassInClassesC"));

		assertTrue(cantLoadClass("evymind.vapor.app.Configuration"));

		Class<?> clazzA = loader.loadClass("org.acme.webapp.ClassInJarA");
		assertTrue(clazzA.getField("FROM_PARENT") != null);
	}

	@Test
	public void testSvcAppLoad() throws Exception {
		context.setParentLoaderPriority(false);
		assertTrue(canLoadClass("org.acme.webapp.ClassInJarA"));
		assertTrue(canLoadClass("org.acme.webapp.ClassInJarB"));
		assertTrue(canLoadClass("org.acme.other.ClassInClassesC"));

		assertTrue(cantLoadClass("evymind.vapor.app.Configuration"));

		Class<?> clazzA = loader.loadClass("org.acme.webapp.ClassInJarA");
		try {
			clazzA.getField("FROM_PARENT");
			assertTrue(false);
		} catch (NoSuchFieldException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testExposedClass() throws Exception {
		String[] oldSC = context.getServerClasses();
		String[] newSC = new String[oldSC.length + 1];
		newSC[0] = "-evymind.vapor.app.Configuration";
		System.arraycopy(oldSC, 0, newSC, 1, oldSC.length);
		context.setServerClasses(newSC);

		assertTrue(canLoadClass("org.acme.webapp.ClassInJarA"));
		assertTrue(canLoadClass("org.acme.webapp.ClassInJarB"));
		assertTrue(canLoadClass("org.acme.other.ClassInClassesC"));

		assertTrue(canLoadClass("evymind.vapor.app.Configuration"));
		assertTrue(cantLoadClass("evymind.vapor.app.JarScanner"));
	}

	@Test
	public void testSystemServerClass() throws Exception {
		String[] oldServC = context.getServerClasses();
		String[] newServC = new String[oldServC.length + 1];
		newServC[0] = "evymind.vapor.app.Configuration";
		System.arraycopy(oldServC, 0, newServC, 1, oldServC.length);
		context.setServerClasses(newServC);

		String[] oldSysC = context.getSystemClasses();
		String[] newSysC = new String[oldSysC.length + 1];
		newSysC[0] = "evymind.vapor.app.";
		System.arraycopy(oldSysC, 0, newSysC, 1, oldSysC.length);
		context.setSystemClasses(newSysC);

		assertTrue(canLoadClass("org.acme.webapp.ClassInJarA"));
		assertTrue(canLoadClass("org.acme.webapp.ClassInJarB"));
		assertTrue(canLoadClass("org.acme.other.ClassInClassesC"));

		assertTrue(cantLoadClass("evymind.vapor.app.Configuration"));
		assertTrue(cantLoadClass("evymind.vapor.app.JarScanner"));
	}

	@Test
	public void testResources() throws Exception {
		List<URL> resources;

		context.setParentLoaderPriority(false);
		resources = toList(loader.getResources("org/acme/resource.txt"));
		assertEquals(3, resources.size());
		assertEquals(0, resources.get(0).toString().indexOf("jar:file:"));
		assertEquals(-1, resources.get(1).toString().indexOf("test-classes"));
		assertEquals(0, resources.get(2).toString().indexOf("file:"));

		context.setParentLoaderPriority(true);
		resources = toList(loader.getResources("org/acme/resource.txt"));
		assertEquals(3, resources.size());
		assertEquals(0, resources.get(0).toString().indexOf("file:"));
		assertEquals(0, resources.get(1).toString().indexOf("jar:file:"));
		assertEquals(-1, resources.get(2).toString().indexOf("test-classes"));

		String[] oldServC = context.getServerClasses();
		String[] newServC = new String[oldServC.length + 1];
		newServC[0] = "org.acme.";
		System.arraycopy(oldServC, 0, newServC, 1, oldServC.length);
		context.setServerClasses(newServC);

		context.setParentLoaderPriority(true);
		resources = toList(loader.getResources("org/acme/resource.txt"));
		assertEquals(2, resources.size());
		assertEquals(0, resources.get(0).toString().indexOf("jar:file:"));
		assertEquals(0, resources.get(1).toString().indexOf("file:"));

		context.setServerClasses(oldServC);
		String[] oldSysC = context.getSystemClasses();
		String[] newSysC = new String[oldSysC.length + 1];
		newSysC[0] = "org.acme.";
		System.arraycopy(oldSysC, 0, newSysC, 1, oldSysC.length);
		context.setSystemClasses(newSysC);

		context.setParentLoaderPriority(true);
		resources = toList(loader.getResources("org/acme/resource.txt"));
		assertEquals(1, resources.size());
		assertEquals(0, resources.get(0).toString().indexOf("file:"));
	}

	private List<URL> toList(Enumeration<URL> e) {
		List<URL> list = new ArrayList<URL>();
		while (e != null && e.hasMoreElements())
			list.add(e.nextElement());
		return list;
	}

	private boolean canLoadClass(String clazz) throws ClassNotFoundException {
		return loader.loadClass(clazz) != null;
	}

	private boolean cantLoadClass(String clazz) {
		try {
			return loader.loadClass(clazz) == null;
		} catch (ClassNotFoundException e) {
			return true;
		}
	}
}
