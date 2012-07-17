package evymind.vapor.core.supertcp;

import evymind.vapor.core.Signal;

public class StandardPackageAck implements PackageAck {
	
	private int ackNo;
	private int ackError = -1;
	private AckState ackState;
	private Signal signal;

	public StandardPackageAck(SuperChannelWorker worker, int id) {
		if (!(worker.isSkipAck() && worker.isRemoteSupportsOptions())) {
			signal = new Signal();
		}
		ackNo = id;
	}

	@Override
	public int getAckNo() {
		return ackNo;
	}

	@Override
	public AckState getAckState() {
		return ackState;
	}

	@Override
	public void setAckState(AckState state) {
		this.ackState = state;
	}

	@Override
	public int getAckError() {
		return ackError;
	}

	@Override
	public void setAckError(int noAckError) {
		this.ackError = noAckError;
	}

	@Override
	public Signal getSignal() {
		return this.signal;
	}

}
