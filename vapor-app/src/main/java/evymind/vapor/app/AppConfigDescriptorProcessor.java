package evymind.vapor.app;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import evymind.vapor.app.config.AppDescriptor;

public class AppConfigDescriptorProcessor implements DescriptorProcessor {
	
	private ObjectMapper mapper;
	
	public AppConfigDescriptorProcessor() {
		mapper = new ObjectMapper(new YAMLFactory());
		
		// DeserializationFeature
		// to prevent exception when encountering unknown property:
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// to allow coercion of JSON empty String ("") to null Object value:
		mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
	}

	@Override
	public void process(AppContext appContext, DescriptorContext descriptorContext) throws Exception {
		if (descriptorContext == null) {
			return;
		}
		
		AppDescriptor appDescriptor = mapper.readValue(descriptorContext.getResource().getInputStream(), AppDescriptor.class);
		appDescriptor.configure(appContext);
	}

}
