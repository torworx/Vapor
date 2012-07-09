package evymind.vapor.core.http.netty;

import evymind.vapor.core.http.HttpResponse;
import evymind.vapor.core.http.HttpResponseStatus;

public class HttpResponseWrapper extends HttpMessageWapper implements HttpResponse {

	public HttpResponseWrapper(org.jboss.netty.handler.codec.http.HttpResponse nativeHttpResponse) {
		super(nativeHttpResponse);
	}
	
	public org.jboss.netty.handler.codec.http.HttpResponse getNativeResponse() {
		return (org.jboss.netty.handler.codec.http.HttpResponse) getNativeMessage();
	}

	private HttpResponseStatus status;
	@Override
	public HttpResponseStatus getStatus() {
		if (status != null) {
			return status;
		}
		
		if (getNativeResponse().getStatus() == null) {
			return null;
		}
		
		status = HttpResponseStatus.valueOf(getNativeResponse().getStatus().getCode());
		return status;
	}

	@Override
	public void setStatus(HttpResponseStatus status) {
		if (status == null) {
			getNativeResponse().setStatus(null);
		} else {
			getNativeResponse().setStatus(org.jboss.netty.handler.codec.http.HttpResponseStatus.valueOf(status.getCode()));
		}
		this.status = status;
	}

}
