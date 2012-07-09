package evymind.vapor.app;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import evyframework.common.io.Resource;

public class Metadata {
	
	private static final Logger log = LoggerFactory.getLogger(Metadata.class);

	protected AppDescriptorContext appDescriptorContext;
	protected final List<DescriptorProcessor> descriptorProcessors = Lists.newArrayList();

	protected final List<Resource> appJars = Lists.newArrayList();
	// protected final List<Resource> orderedAppJars = Lists.newArrayList();
	protected final List<Resource> orderedContainerJars = Lists.newArrayList();

	public void resolve(AppContext context) throws Exception {
		log.debug("metadata resolve {}", context);

		for (DescriptorProcessor p : descriptorProcessors) {
			p.process(context, getAppDescriptorContext());
		}
	}

	public void clear() {
		descriptorProcessors.clear();
		appJars.clear();
		// orderedAppJars.clear();
		orderedContainerJars.clear();
	}


	public DescriptorContext getAppDescriptorContext() {
		return appDescriptorContext;
	}
	/**
	 * 
	 * @param acr The app config resource
	 */
	public void setAppDescriptorContext(Resource acr) {
		this.appDescriptorContext = new AppDescriptorContext(acr);
	}

	public void addDescriptorProcessor(DescriptorProcessor processor) {
		descriptorProcessors.add(processor);
	}

	public void addAppJar(Resource resource) {
		appJars.add(resource);
	}

	public List<Resource> getAppJars() {
		return Collections.unmodifiableList(appJars);
	}

	public List<Resource> getOrderedContainerJars() {
		return orderedContainerJars;
	}

	public void addContainerJar(Resource jar) {
		orderedContainerJars.add(jar);
	}
}
