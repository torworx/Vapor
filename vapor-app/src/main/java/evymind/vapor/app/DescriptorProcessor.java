package evymind.vapor.app;

public interface DescriptorProcessor {

	void process (AppContext appContext, DescriptorContext descriptorContext) throws Exception;
}
