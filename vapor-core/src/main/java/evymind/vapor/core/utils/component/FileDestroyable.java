package evymind.vapor.core.utils.component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.io.support.ResourcePatternResolver;
import evyframework.common.io.support.ResourcePatternUtils;
import evyframework.common.io.utils.FileUtils;

public class FileDestroyable implements Destroyable {

	private static final Logger LOG = LoggerFactory.getLogger(FileDestroyable.class);

	private static final ResourcePatternResolver resolver = ResourcePatternUtils.getFileAsDefaultResourcePatternResolver();

	final List<File> _files = new ArrayList<File>();

	public FileDestroyable() {
	}

	public FileDestroyable(String file) throws IOException {
		_files.add(resolver.getResource(file).getFile());
	}

	public FileDestroyable(File file) {
		_files.add(file);
	}

	public void addFile(String file) throws IOException {
		_files.add(resolver.getResource(file).getFile());
	}

	public void addFile(File file) {
		_files.add(file);
	}

	public void addFiles(Collection<File> files) {
		_files.addAll(files);
	}

	public void removeFile(String file) throws IOException {
		_files.remove(resolver.getResource(file).getFile());
	}

	public void removeFile(File file) {
		_files.remove(file);
	}

	public void destroy() {
		for (File file : _files) {
			if (file.exists()) {
				LOG.debug("Destroy {}", file);
				FileUtils.delete(file);
			}
		}
	}

}
