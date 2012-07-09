package evymind.vapor.core;


public interface TransportChannel extends Transport {
	
	void setServerLocator(ServerLocator serverLocator);
	
	boolean probe(ServerLocator serverLocator);
	
	void probeAll();
	
	void dispatch(VaporBuffer request, VaporBuffer response);

	void dispatch(Message message);
	
	void beforeDispatch(Message message);
}
