package evymind.vapor.server;

import evymind.vapor.core.Message;
import evymind.vapor.core.Transport;

public interface ServiceResponse {
	
	Transport getTransport();
	
	Message getMessage();
}
