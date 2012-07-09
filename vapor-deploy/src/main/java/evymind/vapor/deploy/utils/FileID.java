package evymind.vapor.deploy.utils;

import java.io.File;

/**
 * Simple, yet surprisingly common utility methods for identifying various file types commonly seen and worked with in a
 * deployment scenario.
 */
public class FileID {
	/**
	 * Is the path a App Archive?
	 * 
	 * @param path
	 *            the path to test.
	 * @return True if a .sar or .jar or exploded app directory
	 */
	public static boolean isAppArchive(File path) {
		if (path.isFile()) {
			String name = path.getName().toLowerCase();
			return (name.endsWith(".sar") || name.endsWith(".jar"));
		}

		// is directory
		File appConfig = new File(path, "app.yml");
		return appConfig.exists() && appConfig.isFile();
	}

	/**
	 * Is the path a App Archive File (not directory)
	 * 
	 * @param path
	 *            the path to test.
	 * @return True if a .sar or .jar file.
	 */
	public static boolean isAppArchiveFile(File path) {
		if (!path.isFile()) {
			return false;
		}

		String name = path.getName().toLowerCase();
		return (name.endsWith(".sar") || name.endsWith(".jar"));
	}

	public static boolean isXmlFile(File path) {
		if (!path.isFile()) {
			return false;
		}

		String name = path.getName().toLowerCase();
		return name.endsWith(".xml");
	}
}