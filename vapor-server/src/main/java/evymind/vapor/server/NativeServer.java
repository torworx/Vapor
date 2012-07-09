package evymind.vapor.server;

public interface NativeServer {
	
	void start();
	
	void stop();
	
	boolean isStopped();
	
	int getPort();

	void setPort(int port);
}
