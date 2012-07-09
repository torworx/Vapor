package evymind.vapor.app;

import evyframework.common.io.Resource;

public abstract class DescriptorContext {
	
	private Resource resource;

	public DescriptorContext() {
		super();
	}

	public DescriptorContext(Resource resource) {
		super();
		this.resource = resource;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

}
