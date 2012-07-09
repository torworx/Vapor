package evymind.vapor.core.http.netty;

import evymind.vapor.core.http.HttpMethod;
import evymind.vapor.core.http.HttpRequest;

public class HttpRequestWrapper extends HttpMessageWapper implements HttpRequest {

	public HttpRequestWrapper(org.jboss.netty.handler.codec.http.HttpRequest nativeRequest) {
		super(nativeRequest);
	}
	
	public org.jboss.netty.handler.codec.http.HttpRequest getNativeRequest() {
		return (org.jboss.netty.handler.codec.http.HttpRequest) getNativeMessage();
	}

	
	private HttpMethod method;
	@Override
	public HttpMethod getMethod() {
		if (method != null) {
			return method;
		}
		if (getNativeRequest().getMethod() == null) {
			return null;
		}
		method = HttpMethod.valueOf(getNativeRequest().getMethod().toString());
		return method;
	}

	@Override
	public void setMethod(HttpMethod method) {
		if (method == null) {
			getNativeRequest().setMethod(null);
		} else {
			getNativeRequest().setMethod(org.jboss.netty.handler.codec.http.HttpMethod.valueOf(method.toString()));
		}
		this.method = method;
	}

	@Override
	public String getUri() {
		return getNativeRequest().getUri();
	}

	@Override
	public void setUri(String uri) {
		getNativeRequest().setUri(uri);
	}

}
