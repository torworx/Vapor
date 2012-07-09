package evymind.vapor.client.supertcp;

import evymind.vapor.core.Signal;
import evymind.vapor.core.VaporBuffer;

public class WaitingRequest {
	
	private final int id;
	private final Signal signal;
	
	private VaporBuffer resultData;
	private int resultErrorCode = -1;
	
	public WaitingRequest(int id) {
		this(id, null);
	}
	
	public WaitingRequest(int id, Signal signal) {
		this.id = id;
		if (signal == null) {
			this.signal = new Signal();
		} else {
			this.signal = signal;
		}
	}

	public int getId() {
		return id;
	}

	public Signal getSignal() {
		return signal;
	}

	public VaporBuffer getResultData() {
		return resultData;
	}

	public void setResultData(VaporBuffer resultData) {
		this.resultData = resultData;
	}

	public int getResultErrorCode() {
		return resultErrorCode;
	}

	public void setResultErrorCode(int resultErrorCode) {
		this.resultErrorCode = resultErrorCode;
	}
	
}
