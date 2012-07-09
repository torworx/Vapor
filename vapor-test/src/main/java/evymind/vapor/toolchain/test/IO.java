package evymind.vapor.toolchain.test;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * IO Utilities.
 */
public final class IO {
	public static final int BUFFER_SIZE = 64 * 1024;

	private IO() {
		/* prevent instantiation */
	}

	/**
	 * Copy Reader to Writer out until EOF or exception.
	 */
	public static void copy(Reader in, Writer out) throws IOException {
		char buffer[] = new char[BUFFER_SIZE];
		int len = BUFFER_SIZE;

		while (true) {
			len = in.read(buffer, 0, BUFFER_SIZE);
			if (len == -1) {
				break;
			}
			out.write(buffer, 0, len);
		}
	}

	/**
	 * Read the contents of a file into a String and return it.
	 * 
	 * @param file
	 *            the file to read.
	 * @return the contents of the file.
	 * @throws IOException
	 *             if unable to read the file.
	 */
	public static String readToString(File file) throws IOException {
		FileReader reader = null;
		try {
			reader = new FileReader(file);
			StringWriter writer = new StringWriter();
			copy(reader, writer);
			return writer.toString();
		} finally {
			close(reader);
		}
	}

	/**
	 * closes an {@link Closeable}, and silently ignores exceptions
	 * 
	 * @param c
	 *            the closeable to close
	 */
	public static void close(Closeable c) {
		if (c == null) {
			return;
		}

		try {
			c.close();
		} catch (IOException ignore) {
			/* ignore */
		}
	}

	/**
	 * Copy files or directories
	 * 
	 * @param from
	 * @param to
	 * @throws IOException
	 */
	public static void copy(File from, File to) throws IOException {
		if (from.isDirectory()) {
			copyDir(from, to);
		} else {
			copyFile(from, to);
		}
	}

	public static void copyDir(File from, File to) throws IOException {
		FS.ensureDirExists(to);

		for (File file : from.listFiles(IO.SafeFileFilter.INSTANCE)) {
			copy(file, new File(to, file.getName()));
		}
	}

	public static class SafeFileFilter implements FileFilter {
		public static final SafeFileFilter INSTANCE = new SafeFileFilter();

		public boolean accept(File path) {
			String name = path.getName();
			if (".".equals(name) || "..".equals(name)) {
				return false; // old school.
			}
			return (path.isFile() || path.isDirectory());
		}
	}

	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte buffer[] = new byte[BUFFER_SIZE];
		int len = BUFFER_SIZE;

		while (true) {
			len = in.read(buffer, 0, BUFFER_SIZE);
			if (len < 0) {
				break;
			}
			out.write(buffer, 0, len);
		}
	}

	public static void copyFile(File from, File to) throws IOException {
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(from);
			out = new FileOutputStream(to);
			copy(in, out);
		} finally {
			close(out);
			close(in);
		}
	}
}