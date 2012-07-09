package evymind.vapor.message.envelope.encrypt;

import com.google.common.base.Objects;

import evyframework.common.Assert;
import evyframework.crypto.encrypt.BytesEncryptor;
import evyframework.crypto.encrypt.Encryptors;
import evyframework.crypto.password.PasswordEncoder;
import evyframework.crypto.password.StandardPasswordEncoder;
import evymind.vapor.core.Message;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.message.envelope.MessageEnvelope;

public class AESEncryptionEnvelope extends MessageEnvelope {
	
	private static final PasswordEncoder PASSWORD_ENCODER = new StandardPasswordEncoder();
	
	private static final String SAULT = "a1b2c3d4e5f6d7c8";
	
	private String password;
	
	private BytesEncryptor encryptor;

	public AESEncryptionEnvelope() {
		super();
	}

	public AESEncryptionEnvelope(String marker) {
		super(marker);
	}

	@Override
	protected String getDefaultEnvelopeMarker() {
		return "AES";
	}

	protected String encodePassword(String password) {
		Assert.notNull(password, "No password is configured for " + getEnvelopeMarker());
		return PASSWORD_ENCODER.encode(password);
	}
	
	protected void updateEncryptor() {
		this.encryptor = Encryptors.standard(encodePassword(password), SAULT);
	}

	@Override
	protected void doProcessIncoming(VaporBuffer src, VaporBuffer dest, Message message) {
		byte[] buf = new byte[src.readableBytes()];
		src.readBytes(buf);
		if (encryptor == null) updateEncryptor();
		buf = encryptor.decrypt(buf);
		dest.writeBytes(buf);
	}

	@Override
	protected void doProcessOutcoming(VaporBuffer src, VaporBuffer dest, Message message) {
		byte[] buf = new byte[src.readableBytes()];
		src.readBytes(buf);
		if (encryptor == null) updateEncryptor();
		buf = encryptor.encrypt(buf);
		dest.writeBytes(buf);
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		if (!Objects.equal(this.password, password)) {
			this.password = password;
			updateEncryptor();
		}
	}

}
