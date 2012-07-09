package evymind.vapor.bootstrap;

import java.io.File;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;

/**
 * Smart comparator for filenames, with natural language sorting, and files sorted before sub directories.
 */
public class FilenameComparator implements Comparator<File> {
	public static final FilenameComparator INSTANCE = new FilenameComparator();
	private Collator collator = Collator.getInstance();

	public int compare(File o1, File o2) {
		if (o1.isFile()) {
			if (o2.isFile()) {
				CollationKey key1 = toKey(o1);
				CollationKey key2 = toKey(o2);
				return key1.compareTo(key2);
			} else {
				// Push o2 directories below o1 files
				return -1;
			}
		} else {
			if (o2.isDirectory()) {
				CollationKey key1 = toKey(o1);
				CollationKey key2 = toKey(o2);
				return key1.compareTo(key2);
			} else {
				// Push o2 files above o1 directories
				return 1;
			}
		}
	}

	private CollationKey toKey(File f) {
		return collator.getCollationKey(f.getAbsolutePath());
	}
}