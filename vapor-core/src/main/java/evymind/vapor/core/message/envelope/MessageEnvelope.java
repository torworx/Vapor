package evymind.vapor.core.message.envelope;

import java.util.UUID;

import evyframework.common.StringUtils;
import evymind.vapor.core.Message;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.VaporBuffers;
import evymind.vapor.core.event.component.EventMulticasterWrapper;
import evymind.vapor.core.message.AbstractMessage;

public class MessageEnvelope extends EventMulticasterWrapper {
	
	private String envelopeMarker;
	
	private boolean reuseOriginalBuffer;
	
	public MessageEnvelope() {
		this(null);
	}

	public MessageEnvelope(String marker) {
		super();
		reuseOriginalBuffer = true;
		envelopeMarker = marker == null ? getDefaultEnvelopeMarker() : marker;
	}

	protected void doProcessIncoming(VaporBuffer src, VaporBuffer dest, Message message) {
		dest.writeBytes(src, src.readableBytes());
	}
	
	protected void doProcessOutcoming(VaporBuffer src, VaporBuffer dest, Message message) {
		dest.writeBytes(src, src.readableBytes());
	}
	
	public VaporBuffer processIncoming(Message message, VaporBuffer buffer) {
		multicastEvent(new BeforeEnvelopeProcessedEvent(this, buffer, message, MessageEnvelopeMode.INCOMING));
		VaporBuffer answer = VaporBuffers.dynamicBuffer();
		doProcessIncoming(buffer, answer, message);
		if (isReuseOriginalBuffer()) {
			buffer.clear();
			buffer.writeBytes(answer, answer.readableBytes());
			answer = buffer;
		}
		multicastEvent(new AfterEnvelopeProcessedEvent(this, buffer, message, MessageEnvelopeMode.INCOMING));
		return answer;
	}
	
	public VaporBuffer processOutcoming(Message message, VaporBuffer buffer) {
		multicastEvent(new BeforeEnvelopeProcessedEvent(this, buffer, message, MessageEnvelopeMode.OUTCOMING));
		VaporBuffer answer = VaporBuffers.dynamicBuffer();
		if (message instanceof AbstractMessage) {
			((AbstractMessage<?>) message).writeEnvelopeHeader(this, answer);
		}
		doProcessOutcoming(buffer, answer, message);
		if (isReuseOriginalBuffer()) {
			buffer.clear();
			buffer.writeBytes(answer, answer.readableBytes());
			answer = buffer;
		}
		multicastEvent(new AfterEnvelopeProcessedEvent(this, buffer, message, MessageEnvelopeMode.OUTCOMING));
		return answer;
	}
	
	protected String getDefaultEnvelopeMarker() {
		return UUID.randomUUID().toString();
	}

	public String getEnvelopeMarker() {
		return envelopeMarker;
	}

	public void setEnvelopeMarker(String envelopeMarker) {
		if (StringUtils.hasText(envelopeMarker)) {
			int len = envelopeMarker.length();
			while (len > 255) len >>= 1;
			this.envelopeMarker = envelopeMarker.substring(0, len - 1);
		} else {
			this.envelopeMarker = UUID.randomUUID().toString();
		}

	}

	public boolean isReuseOriginalBuffer() {
		return reuseOriginalBuffer;
	}

	public void setReuseOriginalBuffer(boolean reuseOriginalBuffer) {
		this.reuseOriginalBuffer = reuseOriginalBuffer;
	}

}
