package evymind.vapor.core.supertcp;

import evyframework.common.Assert;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.Buffers;

public class DataReceiver {
	
	private DataTransferListener listener;
	private VaporBuffer data;

	public void start(int total, DataTransferListener listener) {
		this.listener = listener;
		data = Buffers.dynamicBuffer(total);
		fireReceiveProgress(DataTransferState.START, 0);
	}
	
	public boolean isInProgress() {
		return (data != null && data.writable());
	}
	
	public void receive(VaporBuffer buffer) {
		Assert.notNull(data, "Call start before receive");
		Assert.notNull(buffer, "'buffer' must not be null");
		int count = data.writableBytes() < buffer.readableBytes() ? data.writableBytes() : buffer.readableBytes();
		data.writeBytes(buffer, count);
		fireReceiveProgress(DataTransferState.IN_PROGRESS, data.readableBytes());
		if (!data.writable()) {
			fireReceiveProgress(DataTransferState.COMPLETE, data.readableBytes());
			fireReceiveComplete();
			data = null;
		}
	}
	
	protected void fireReceiveProgress(DataTransferState state, int transfered) {
		if (listener != null) {
			listener.receiveProgress(state, transfered, data.capacity());
		}
	}
	
	protected void fireReceiveComplete() {
		if (listener != null) {
			listener.receiveComplete(data);
		}
	}

}
