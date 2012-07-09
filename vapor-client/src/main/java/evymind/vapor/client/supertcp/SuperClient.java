package evymind.vapor.client.supertcp;

import evymind.vapor.core.supertcp.SuperConnection;

public interface SuperClient extends SuperConnection {
	
	void connect(String host, int port) throws InterruptedException;
	
	void connect(String host, int port, int timeoutMillis) throws InterruptedException;

}
