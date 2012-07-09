package evymind.vapor.server.supertcp;

import evymind.vapor.server.Request;
import evymind.vapor.server.Response;

public class RequestEvent {
	
	private final Request request;
	private final Response response;
	
	public RequestEvent(Request request, Response response) {
		this.request = request;
		this.response = response;
	}

	public Request getRequest() {
		return request;
	}

	public Response getResponse() {
		return response;
	}

}
