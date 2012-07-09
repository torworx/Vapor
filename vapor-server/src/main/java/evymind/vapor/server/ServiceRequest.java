package evymind.vapor.server;

import java.util.UUID;

import evymind.vapor.core.Message;
import evymind.vapor.core.Transport;

public interface ServiceRequest {
	
	Connector getConnector();
	
	<T extends Transport> T getTransport();
	
	int getRequestId();
	
	UUID getClientId();
	
	String getRequestInterface();
	
	String getRequestMethod();
	
	Session getSession(boolean create);
	
	Session getSession();
	
	String getRemoteAddress();
	
	int getRemotePort();
	
	Message getMessage();

}
