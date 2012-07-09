package evymind.vapor.integration.envelope;

import evymind.vapor.core.MessageFactory;
import evymind.vapor.core.message.bin.BinMessageFactory;
import evymind.vapor.core.message.envelope.MessageEnvelope;

public class EnvelopeTestBase {
	
	MessageFactory<?> messageFactory;
	
	protected void initMessageFactory() {
		messageFactory = new BinMessageFactory();
		messageFactory.addEnvelope(new MessageEnvelope("SimpleCopyEnvelope"));
	}

}
