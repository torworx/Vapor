package evymind.vapor.bootstrap;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/*-------------------------------------------*/
/**
 * <p>
 * Main bootstrap class. This class is intended to be the main class listed in the MANIFEST.MF of the bootstrap.jar archive. It
 * allows an application to be started with the command "java -jar bootstrap.jar".
 * </p>
 * 
 * <p>
 * The behaviour of Main is controlled by the parsing of the {@link Environment} "evymind/vapor/bootstrap/bootstrap.env" file
 * obtained as a resource or file.
 * </p>
 */
public class Main {
	private static final int EXIT_USAGE = 1;
	private static final int ERR_LOGGING = -1;
	private static final int ERR_INVOKE_MAIN = -2;
	private static final int ERR_NOT_STOPPED = -4;
	private static final int ERR_UNKNOWN = -5;
	private boolean showUsage = false;
	private boolean dumpVersions = false;
	private boolean listConfig = false;
	private boolean listOptions = false;
	private boolean dryRun = false;
	private boolean exec = false;
	private final Environment environment = new Environment();
	private final Set<String> sysProps = new HashSet<String>();
	private final List<String> jvmArgs = new ArrayList<String>();
	private String bootstrapConfig = null;

	private String vaporHome;

	public static void main(String[] args) {
		try {
			Main main = new Main();
			List<String> arguments = main.expandCommandLine(args);
			List<String> configs = main.processCommandLine(arguments);
			if (configs != null)
				main.bootstrap(configs);
		} catch (Throwable e) {
			usageExit(e, ERR_UNKNOWN);
		}
	}

	Main() throws IOException {
		this.vaporHome = System.getProperty("vapor.home", ".");
		this.vaporHome = new File(this.vaporHome).getCanonicalPath();
	}

	public List<String> expandCommandLine(String[] args) throws Exception {
		List<String> arguments = new ArrayList<String>();

		// add the command line args and look for bootstrap.ini args
		boolean ini = false;
		for (String arg : args) {
			if (arg.startsWith("--ini=") || arg.equals("--ini")) {
				ini = true;
				if (arg.length() > 6) {
					arguments.addAll(loadBootstrapIni(new File(arg.substring(6))));
					continue;
				}
			} else if (arg.startsWith("--environment=")) {
				this.bootstrapConfig = arg.substring(9);
			} else {
				arguments.add(arg);
			}
		}

		// if no non-option inis, add the bootstrap.ini and bootstrap.d
		if (!ini) {
			arguments.addAll(0, parseBootstrapIniFiles());
		}

		return arguments;
	}

	List<String> parseBootstrapIniFiles() {
		List<String> ini_args = new ArrayList<String>();
		File bootstrap_ini = new File(this.vaporHome, "bootstrap.ini");
		if (bootstrap_ini.exists())
			ini_args.addAll(loadBootstrapIni(bootstrap_ini));

		File bootstrap_d = new File(this.vaporHome, "bootstrap.d");
		if (bootstrap_d.isDirectory()) {
			File[] inis = bootstrap_d.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".ini");
				}
			});
			Arrays.sort(inis);
			for (File i : inis)
				ini_args.addAll(loadBootstrapIni(i));
		}
		return ini_args;
	}

	public List<String> processCommandLine(List<String> arguments) throws Exception {
		// The XML Configuration Files to initialize with
		List<String> configs = new ArrayList<String>();

		// Process the arguments
		int startup = 0;
		for (String arg : arguments) {
			if ("--help".equals(arg) || "-?".equals(arg)) {
				this.showUsage = true;
				continue;
			}

			if ("--stop".equals(arg)) {
				int port = Integer.parseInt(Environment.getProperty("STOP.PORT", "-1"));
				String key = Environment.getProperty("STOP.KEY", null);
				stop(port, key);
				return null;
			}

			if ("--version".equals(arg) || "-v".equals(arg) || "--info".equals(arg)) {
				dumpVersions = true;
				continue;
			}

			if ("--list-modes".equals(arg) || "--list-options".equals(arg)) {
				listOptions = true;
				continue;
			}

			if ("--list-environment".equals(arg)) {
				listConfig = true;
				continue;
			}

			if ("--exec-print".equals(arg) || "--dry-run".equals(arg)) {
				dryRun = true;
				continue;
			}

			if ("--exec".equals(arg)) {
				this.exec = true;
				continue;
			}

			// Special internal indicator that vapor was started by the vapor.sh Daemon
			if ("--daemon".equals(arg)) {
				File bootstrapDir = new File(System.getProperty("vapor.logs", "logs"));
				if (!bootstrapDir.exists() || !bootstrapDir.canWrite())
					bootstrapDir = new File(".");
				File bootstrapLog = new File(bootstrapDir, "bootstrap.log");
				if (!bootstrapLog.exists() && !bootstrapLog.createNewFile()) {
					// Output about error is lost in majority of cases.
					System.err.println("Unable to create: " + bootstrapLog.getAbsolutePath());
					// Toss a unique exit code indicating this failure.
					usageExit(ERR_LOGGING);
				}

				if (!bootstrapLog.canWrite()) {
					// Output about error is lost in majority of cases.
					System.err.println("Unable to write to: " + bootstrapLog.getAbsolutePath());
					// Toss a unique exit code indicating this failure.
					usageExit(ERR_LOGGING);
				}
				PrintStream logger = new PrintStream(new FileOutputStream(bootstrapLog, false));
				System.setOut(logger);
				System.setErr(logger);
				System.out.println("Establishing bootstrap.log on " + new Date());
				continue;
			}

			if (arg.startsWith("--pre=")) {
				configs.add(startup++, arg.substring(6));
				continue;
			}

			if (arg.startsWith("-D")) {
				String[] assign = arg.substring(2).split("=", 2);
				this.sysProps.add(assign[0]);
				switch (assign.length) {
				case 2:
					System.setProperty(assign[0], assign[1]);
					break;
				case 1:
					System.setProperty(assign[0], "");
					break;
				default:
					break;
				}
				continue;
			}

			if (arg.startsWith("-")) {
				this.jvmArgs.add(arg);
				continue;
			}

			// Is this a Property?
			if (arg.indexOf('=') >= 0) {
				String[] assign = arg.split("=", 2);

				switch (assign.length) {
				case 2:
					if ("OPTIONS".equals(assign[0])) {
						String opts[] = assign[1].split(",");
						for (String opt : opts)
							this.environment.addActiveOption(opt);
					} else {
						this.environment.setProperty(assign[0], assign[1]);
					}
					break;
				case 1:
					this.environment.setProperty(assign[0], null);
					break;
				default:
					break;
				}

				continue;
			}

			// Anything else is considered an XML file.
			if (configs.contains(arg)) {
				System.out.println("WARN: Argument '" + arg + "' specified multiple times. Check bootstrap.ini?");
				System.out.println("Use \"java -jar bootstrap.jar --help\" for more information.");
			}
			configs.add(arg);
		}

		return configs;
	}

	private void usage() {
		String usageResource = "evymind/vapor/bootstrap/usage.txt";
		InputStream usageStream = getClass().getClassLoader().getResourceAsStream(usageResource);

		if (usageStream == null) {
			System.err.println("ERROR: detailed usage resource unavailable");
			usageExit(EXIT_USAGE);
		}

		BufferedReader buf = null;
		try {
			buf = new BufferedReader(new InputStreamReader(usageStream));
			String line;

			while ((line = buf.readLine()) != null) {
				if (line.endsWith("@") && line.indexOf('@') != line.lastIndexOf('@')) {
					String indent = line.substring(0, line.indexOf("@"));
					String info = line.substring(line.indexOf('@'), line.lastIndexOf('@'));

					if (info.equals("@OPTIONS")) {
						List<String> sortedOptions = new ArrayList<String>();
						sortedOptions.addAll(this.environment.getSectionIds());
						Collections.sort(sortedOptions);

						for (String option : sortedOptions) {
							if ("*".equals(option) || option.trim().length() == 0)
								continue;
							System.out.print(indent);
							System.out.println(option);
						}
					} else if (info.equals("@CONFIGS")) {
						File etc = new File(System.getProperty("vapor.home", "."), "etc");
						if (!etc.exists() || !etc.isDirectory()) {
							System.out.print(indent);
							System.out.println("Unable to find/list " + etc);
							continue;
						}

						File configs[] = etc.listFiles(new FileFilter() {
							public boolean accept(File path) {
								if (!path.isFile()) {
									return false;
								}

								String name = path.getName().toLowerCase();
								return (name.startsWith("vapor") && name.endsWith(".ecs"));
							}
						});

						List<File> configFiles = new ArrayList<File>();
						configFiles.addAll(Arrays.asList(configs));
						Collections.sort(configFiles);

						for (File configFile : configFiles) {
							System.out.print(indent);
							System.out.print("etc/");
							System.out.println(configFile.getName());
						}
					} else if (info.equals("@BOOTSTRAPINI")) {
						List<String> ini = loadBootstrapIni(new File(this.vaporHome, "bootstrap.ini"));
						if (ini != null && ini.size() > 0) {
							for (String a : ini) {
								System.out.print(indent);
								System.out.println(a);
							}
						} else {
							System.out.print(indent);
							System.out.println("none");
						}
					}
				} else {
					System.out.println(line);
				}
			}
		} catch (IOException e) {
			usageExit(e, EXIT_USAGE);
		} finally {
			close(buf);
		}
		System.exit(EXIT_USAGE);
	}

	public void invokeMain(ClassLoader classloader, String classname, List<String> args) throws IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
		Class<?> invoked_class = null;

		try {
			invoked_class = classloader.loadClass(classname);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		if (Environment.isDebug() || invoked_class == null) {
			if (invoked_class == null) {
				System.err.println("ClassNotFound: " + classname);
			} else {
				System.err.println(classname + " " + invoked_class.getPackage().getImplementationVersion());
			}

			if (invoked_class == null) {
				usageExit(ERR_INVOKE_MAIN);
				return;
			}
		}

		String argArray[] = args.toArray(new String[0]);

		Class<?>[] method_param_types = new Class[] { argArray.getClass() };

		Method main = invoked_class.getDeclaredMethod("main", method_param_types);
		Object[] method_params = new Object[] { argArray };
		main.invoke(null, method_params);
	}


	public static void close(Closeable c) {
		if (c == null) {
			return;
		}
		try {
			c.close();
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}


	public void bootstrap(List<String> configs) throws FileNotFoundException, IOException, InterruptedException {
		// Setup Start / Stop Monitoring
		int port = Integer.parseInt(Environment.getProperty("STOP.PORT", "-1"));
		String key = Environment.getProperty("STOP.KEY", null);
		Monitor monitor = new Monitor(port, key);

		// Load potential Environment (bootstrap.env)
		List<String> configuredConfigs = loadConfig(configs);

		// No environment defined in bootstrap.env or command line. Can't execute.
		if (configuredConfigs.isEmpty()) {
			throw new FileNotFoundException("No configuration files specified in bootstrap.env or command line.");
		}

		// Normalize the environment options passed on the command line.
		configuredConfigs = resolveConfigs(configuredConfigs);

		// Get Desired Classpath based on user provided Active Options.
		Classpath classpath = this.environment.getActiveClasspath();

		System.setProperty("java.class.path", classpath.toString());
		ClassLoader cl = classpath.getClassLoader();
		if (Environment.isDebug()) {
			System.err.println("java.class.path=" + System.getProperty("java.class.path"));
			System.err.println("vapor.home=" + System.getProperty("vapor.home"));
			System.err.println("java.home=" + System.getProperty("java.home"));
			System.err.println("java.io.tmpdir=" + System.getProperty("java.io.tmpdir"));
			System.err.println("java.class.path=" + classpath);
			System.err.println("classloader=" + cl);
			System.err.println("classloader.parent=" + cl.getParent());
			System.err.println("properties=" + Environment.getProperties());
		}

		// Show the usage information and return
		if (this.showUsage) {
			usage();
			return;
		}

		// Show the version information and return
		if (dumpVersions) {
			showClasspathWithVersions(classpath);
			return;
		}

		// Show all options with version information
		if (listOptions) {
			showAllOptionsWithVersions(classpath);
			return;
		}

		if (listConfig) {
			listConfig();
			return;
		}

		// Show Command Line to execute Vapor
		if (dryRun) {
			CommandLineBuilder cmd = buildCommandLine(classpath, configuredConfigs);
			System.out.println(cmd.toString());
			return;
		}

		// execute Vapor in another JVM
		if (this.exec) {
			CommandLineBuilder cmd = buildCommandLine(classpath, configuredConfigs);

			ProcessBuilder pbuilder = new ProcessBuilder(cmd.getArgs());
			final Process process = pbuilder.start();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					Environment.debug("Destroying " + process);
					process.destroy();
				}
			});

			copyInThread(process.getErrorStream(), System.err);
			copyInThread(process.getInputStream(), System.out);
			copyInThread(System.in, process.getOutputStream());
			monitor.setProcess(process);
			process.waitFor();

			return;
		}

		if (this.jvmArgs.size() > 0 || this.sysProps.size() > 0) {
			System.err.println("WARNING: System properties and/or JVM args set.  Consider using --dry-run or --exec");
		}

		// Set current context class loader to what is selected.
		Thread.currentThread().setContextClassLoader(cl);

		// Invoke the Main Class
		try {
			// Get main class as defined in bootstrap.env
			String classname = this.environment.getMainClassname();

			// Check for override of start class (via "vapor.server" property)
			String mainClass = System.getProperty("vapor.server");
			if (mainClass != null) {
				classname = mainClass;
			}

			// Check for override of start class (via "main.class" property)
			mainClass = System.getProperty("main.class");
			if (mainClass != null) {
				classname = mainClass;
			}

			Environment.debug("main.class=" + classname);

			invokeMain(cl, classname, configuredConfigs);
		} catch (Exception e) {
			usageExit(e, ERR_INVOKE_MAIN);
		}
	}

	private void copyInThread(final InputStream in, final OutputStream out) {
		new Thread(new Runnable() {
			public void run() {
				try {
					byte[] buf = new byte[1024];
					int len = in.read(buf);
					while (len > 0) {
						out.write(buf, 0, len);
						len = in.read(buf);
					}
				} catch (IOException e) {
					// e.printStackTrace();
				}
			}

		}).start();
	}

	private String resolveConfig(String configFilename) throws FileNotFoundException {
		if (!configFilename.toLowerCase().endsWith(".ecs")) {
			// Nothing to resolve.
			return configFilename;
		}

		File configFile = new File(configFilename);
		if (configFile.exists() && configFile.isFile()) {
			return configFile.getAbsolutePath();
		}

		configFile = new File(this.vaporHome, fixPath(configFilename));
		if (configFile.exists() && configFile.isFile()) {
			return configFile.getAbsolutePath();
		}

		configFile = new File(this.vaporHome, fixPath("etc/" + configFilename));
		if (configFile.exists() && configFile.isFile()) {
			return configFile.getAbsolutePath();
		}

		throw new FileNotFoundException("Unable to find Environment: " + configFilename);
	}

	CommandLineBuilder buildCommandLine(Classpath classpath, List<String> configs) throws IOException {
		CommandLineBuilder cmd = new CommandLineBuilder(findJavaBin());

		for (String x : this.jvmArgs) {
			cmd.addArg(x);
		}
		cmd.addRawArg("-Dvapor.home=" + this.vaporHome);
		for (String p : this.sysProps) {
			String v = System.getProperty(p);
			cmd.addEqualsArg("-D" + p, v);
		}
		cmd.addArg("-cp");
		cmd.addRawArg(classpath.toString());
		cmd.addRawArg(this.environment.getMainClassname());

		// Check if we need to pass properties as a file
		Properties properties = Environment.getProperties();
		if (properties.size() > 0) {
			File prop_file = File.createTempFile("bootstrap", ".properties");
			if (!dryRun)
				prop_file.deleteOnExit();
			properties.store(new FileOutputStream(prop_file), "bootstrap.jar properties");
			cmd.addArg(prop_file.getAbsolutePath());
		}

		for (String config : configs) {
			cmd.addRawArg(config);
		}
		return cmd;
	}

	private String findJavaBin() {
		File javaHome = new File(System.getProperty("java.home"));
		if (!javaHome.exists()) {
			return null;
		}

		File javabin = findExecutable(javaHome, "bin/java");
		if (javabin != null) {
			return javabin.getAbsolutePath();
		}

		javabin = findExecutable(javaHome, "bin/java.exe");
		if (javabin != null) {
			return javabin.getAbsolutePath();
		}

		return "java";
	}

	private File findExecutable(File root, String path) {
		String npath = path.replace('/', File.separatorChar);
		File exe = new File(root, npath);
		if (!exe.exists()) {
			return null;
		}
		return exe;
	}

	private void showAllOptionsWithVersions(Classpath classpath) {
		Set<String> sectionIds = this.environment.getSectionIds();

		StringBuffer msg = new StringBuffer();
		msg.append("There ");
		if (sectionIds.size() > 1) {
			msg.append("are ");
		} else {
			msg.append("is ");
		}
		msg.append(String.valueOf(sectionIds.size()));
		msg.append(" OPTION");
		if (sectionIds.size() > 1) {
			msg.append("s");
		}
		msg.append(" available to use.");
		System.out.println(msg);
		System.out
				.println("Each option is listed along with associated available classpath entries,  in the order that they would appear from that mode.");
		System.out.println("Note: If using multiple options (eg: 'Server,servlet,webapp,jms,jmx') "
				+ "then overlapping entries will not be repeated in the eventual classpath.");
		System.out.println();
		System.out.printf("${vapor.home} = %s%n", this.vaporHome);
		System.out.println();

		for (String sectionId : sectionIds) {
			if (Environment.DEFAULT_SECTION.equals(sectionId)) {
				System.out.println("GLOBAL option (Prepended Entries)");
			} else if ("*".equals(sectionId)) {
				System.out.println("GLOBAL option (Appended Entries) (*)");
			} else {
				System.out.printf("Option [%s]", sectionId);
				if (Character.isUpperCase(sectionId.charAt(0))) {
					System.out.print(" (Aggregate)");
				}
				System.out.println();
			}
			System.out.println("-------------------------------------------------------------");

			Classpath sectionCP = this.environment.getSectionClasspath(sectionId);

			if (sectionCP.isEmpty()) {
				System.out.println("Empty option, no classpath entries active.");
				System.out.println();
				continue;
			}

			int i = 0;
			for (File element : sectionCP.getElements()) {
				String elementPath = element.getAbsolutePath();
				if (elementPath.startsWith(this.vaporHome)) {
					elementPath = "${vapor.home}" + elementPath.substring(this.vaporHome.length());
				}
				System.out.printf("%2d: %20s | %s\n", i++, getVersion(element), elementPath);
			}

			System.out.println();
		}
	}

	private void showClasspathWithVersions(Classpath classpath) {
		// Iterate through active classpath, and fetch Implementation Version from each entry (if present)
		// to dump to end user.

		System.out.println("Active Options: " + this.environment.getActiveOptions());

		if (classpath.count() == 0) {
			System.out.println("No version information available show.");
			return;
		}

		System.out.println("Version Information on " + classpath.count() + " entr"
				+ ((classpath.count() > 1) ? "ies" : "y") + " in the classpath.");
		System.out.println("Note: order presented here is how they would appear on the classpath.");
		System.out
				.println("      changes to the OPTIONS=[option,option,...] command line option will be reflected here.");

		int i = 0;
		for (File element : classpath.getElements()) {
			String elementPath = element.getAbsolutePath();
			if (elementPath.startsWith(this.vaporHome)) {
				elementPath = "${vapor.home}" + elementPath.substring(this.vaporHome.length());
			}
			System.out.printf("%2d: %20s | %s\n", i++, getVersion(element), elementPath);
		}
	}

	private String fixPath(String path) {
		return path.replace('/', File.separatorChar);
	}

	private String getVersion(File element) {
		if (element.isDirectory()) {
			return "(dir)";
		}

		if (element.isFile()) {
			String name = element.getName().toLowerCase();
			if (name.endsWith(".jar")) {
				return JarVersion.getVersion(element);
			}

			if (name.endsWith(".zip")) {
				return getZipVersion(element);
			}
		}

		return "";
	}

	private String getZipVersion(File element) {
		// TODO - find version in zip file. Look for META-INF/MANIFEST.MF ?
		return "";
	}

	private List<String> resolveConfigs(List<String> configs) throws FileNotFoundException {
		List<String> ret = new ArrayList<String>();
		for (String config : configs) {
			ret.add(resolveConfig(config));
		}

		return ret;
	}

	private void listConfig() {
		InputStream cfgstream = null;
		try {
			cfgstream = getConfigStream();
			byte[] buf = new byte[4096];

			int len = 0;

			while (len >= 0) {
				len = cfgstream.read(buf);
				if (len > 0)
					System.out.write(buf, 0, len);
			}
		} catch (Exception e) {
			usageExit(e, ERR_UNKNOWN);
		} finally {
			close(cfgstream);
		}
	}

	/**
	 * Load Configuration.
	 * 
	 * No specific configuration is real until a {@link Environment#getCombinedClasspath(java.util.Collection)} is used to
	 * execute the {@link Class} specified by {@link Environment#getMainClassname()} is executed.
	 * 
	 * @param configs
	 *            the command line specified configuration options.
	 * @return the list of configurations arriving via command line and bootstrap.env choices.
	 */
	private List<String> loadConfig(List<String> configs) {
		InputStream cfgstream = null;
		try {
			// Pass in xmls.size into Environment so that conditions based on "nargs" work.
			this.environment.setArgCount(configs.size());

			cfgstream = getConfigStream();

			// parse the environment
			this.environment.parse(cfgstream);

			this.vaporHome = Environment.getProperty("vapor.home", this.vaporHome);
			if (this.vaporHome != null) {
				this.vaporHome = new File(this.vaporHome).getCanonicalPath();
				System.setProperty("vapor.home", this.vaporHome);
			}

			// Collect the configured xml configurations.
			List<String> ret = new ArrayList<String>();
			ret.addAll(configs); // add command line provided xmls first.
			for (String config : this.environment.getConfigs()) {
				// add xmlconfigs arriving via bootstrap.env
				if (!ret.contains(config)) {
					ret.add(config);
				}
			}

			return ret;
		} catch (Exception e) {
			usageExit(e, ERR_UNKNOWN);
			return null; // never executed (just here to satisfy javac compiler)
		} finally {
			close(cfgstream);
		}
	}

	private InputStream getConfigStream() throws FileNotFoundException {
		String config = this.bootstrapConfig;
		if (config == null || config.length() == 0) {
			config = System.getProperty("BOOTSTRAP", "evymind/vapor/bootstrap/bootstrap.env");
		}

		Environment.debug("environment=" + config);

		// Look up environment as resource first.
		InputStream cfgstream = getClass().getClassLoader().getResourceAsStream(config);

		// resource not found, try filesystem next
		if (cfgstream == null) {
			cfgstream = new FileInputStream(config);
		}

		return cfgstream;
	}

	/**
	 * Stop a running vapor instance.
	 */
	public void stop(int port, String key) {
		int _port = port;
		String _key = key;

		try {
			if (_port <= 0) {
				System.err.println("STOP.PORT system property must be specified");
			}
			if (_key == null) {
				_key = "";
				System.err.println("STOP.KEY system property must be specified");
				System.err.println("Using empty key");
			}

			Socket s = new Socket(InetAddress.getByName("127.0.0.1"), _port);
			try {
				OutputStream out = s.getOutputStream();
				out.write((_key + "\r\nstop\r\n").getBytes());
				out.flush();
			} finally {
				s.close();
			}
		} catch (ConnectException e) {
			usageExit(e, ERR_NOT_STOPPED);
		} catch (Exception e) {
			usageExit(e, ERR_UNKNOWN);
		}
	}

	static void usageExit(Throwable t, int exit) {
		t.printStackTrace(System.err);
		System.err.println();
		System.err.println("Usage: java -jar bootstrap.jar [options] [properties] [configs]");
		System.err.println("       java -jar bootstrap.jar --help  # for more information");
		System.exit(exit);
	}

	static void usageExit(int exit) {
		System.err.println();
		System.err.println("Usage: java -jar bootstrap.jar [options] [properties] [configs]");
		System.err.println("       java -jar bootstrap.jar --help  # for more information");
		System.exit(exit);
	}

	/**
	 * Convert a bootstrap.ini format file into an argument list.
	 */
	static List<String> loadBootstrapIni(File ini) {
		File startIniFile = ini;
		if (!startIniFile.exists()) {
			if (ini != null) {
				System.err.println("Warning - can't find ini file: " + ini);
			}
			// No bootstrap.ini found, skip load.
			return Collections.emptyList();
		}

		List<String> args = new ArrayList<String>();

		FileReader reader = null;
		BufferedReader buf = null;
		try {
			reader = new FileReader(ini);
			buf = new BufferedReader(reader);

			String arg;
			while ((arg = buf.readLine()) != null) {
				arg = arg.trim();
				if (arg.length() == 0 || arg.startsWith("#")) {
					continue;
				}
				args.add(arg);
			}
		} catch (IOException e) {
			usageExit(e, ERR_UNKNOWN);
		} finally {
			Main.close(buf);
			Main.close(reader);
		}

		return args;
	}

	void addJvmArgs(List<String> jvmArgs) {
		this.jvmArgs.addAll(jvmArgs);
	}
}
