package evymind.vapor.core;

public interface TCPTransport extends Transport {
	
	String getRemoteAddress();
	
	int getRemotePort();

}
