package evymind.vapor.server;

public class RequestHolder {
	
	private static final ThreadLocal<Request> requestObjectHolder = new ThreadLocal<Request>();
	
	public static ServiceRequest getServiceRequest() {
		return requestObjectHolder.get();
	}

	public static void setServiceRequest(Request request) {
		requestObjectHolder.set(request);
	}
}
