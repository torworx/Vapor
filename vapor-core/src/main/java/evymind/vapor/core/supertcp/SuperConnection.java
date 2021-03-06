package evymind.vapor.core.supertcp;

import evymind.vapor.core.VaporBuffer;

public interface SuperConnection {

	void disconnect();
	
	boolean isConnected();
	
	String getRemoteAddress();
	
	int getRemotePort();
	
	void writeBuffer(VaporBuffer buffer);
	
	void writeBuffer(VaporBuffer buffer, int chunkSize);
}
