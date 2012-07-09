package evymind.vapor.core.supertcp;

import evymind.vapor.core.VaporBuffer;

public interface DataTransferListener {
	
	void receiveProgress(DataTransferState state, int transfered, int total);
	
	void receiveComplete(VaporBuffer data);
}
