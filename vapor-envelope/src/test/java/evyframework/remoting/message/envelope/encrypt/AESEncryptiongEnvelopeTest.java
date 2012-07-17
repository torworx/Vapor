package evyframework.remoting.message.envelope.encrypt;

import org.junit.Assert;
import org.junit.Test;

import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.VaporBuffers;
import evymind.vapor.message.envelope.encrypt.AESEncryptionEnvelope;

public class AESEncryptiongEnvelopeTest {
	
	@Test
	public void testProcess() {
		AESEncryptionEnvelope envelope = new AESEncryptionEnvelope();
		envelope.setPassword("password");
		String msg = "Hello World！你好，世界！";
		VaporBuffer buffer = VaporBuffers.dynamicBuffer();
		buffer.writeString(msg);
		envelope.processOutcoming(null, buffer);
		envelope.processIncoming(null, buffer);
		Assert.assertEquals(msg, buffer.readString());
	}

}
