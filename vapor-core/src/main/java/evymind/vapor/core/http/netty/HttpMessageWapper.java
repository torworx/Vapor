package evymind.vapor.core.http.netty;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.netty.ChannelBufferExposer;
import evymind.vapor.core.buffer.netty.ChannelBufferWrapper;
import evymind.vapor.core.http.HttpMessage;
import evymind.vapor.core.http.HttpVersion;

public class HttpMessageWapper implements HttpMessage {

	private final org.jboss.netty.handler.codec.http.HttpMessage nativeMessage;

	public HttpMessageWapper(org.jboss.netty.handler.codec.http.HttpMessage nativeMessage) {
		super();
		this.nativeMessage = nativeMessage;
	}
	
	protected org.jboss.netty.handler.codec.http.HttpMessage getNativeMessage() {
		return this.nativeMessage;
	}

	public String getHeader(String name) {
		return nativeMessage.getHeader(name);
	}

	public List<String> getHeaders(String name) {
		return nativeMessage.getHeaders(name);
	}

	public List<Entry<String, String>> getHeaders() {
		return nativeMessage.getHeaders();
	}

	public boolean containsHeader(String name) {
		return nativeMessage.containsHeader(name);
	}

	public Set<String> getHeaderNames() {
		return nativeMessage.getHeaderNames();
	}

	private HttpVersion version;
	public HttpVersion getProtocolVersion() {
		if (version != null) {
			return version;
		}
		if (nativeMessage.getProtocolVersion() == null) {
			return null;
		}
		version = HttpVersion.valueOf(nativeMessage.getProtocolVersion().toString());
		return version;
	}

	public void setProtocolVersion(HttpVersion version) {
		if (version == null) {
			nativeMessage.setProtocolVersion(null);
		} else {
			nativeMessage.setProtocolVersion(org.jboss.netty.handler.codec.http.HttpVersion.valueOf(version.toString()));
		}
		this.version = version;
	}

	private VaporBuffer content;
	public VaporBuffer getContent() {
		if (content != null) {
			return content;
		}
		if (nativeMessage.getContent() == null) {
			return null;
		}
		content = new ChannelBufferWrapper(nativeMessage.getContent());
		return content;
	}

	public void setContent(VaporBuffer content) {
		if (content == null) {
			nativeMessage.setContent(null);
		} else {
			nativeMessage.setContent(((ChannelBufferExposer)content).channelBuffer());
		}
		this.content = content;
	}

	public void addHeader(String name, Object value) {
		nativeMessage.addHeader(name, value);
	}

	public void setHeader(String name, Object value) {
		nativeMessage.setHeader(name, value);
	}

	public void setHeader(String name, Iterable<?> values) {
		nativeMessage.setHeader(name, values);
	}

	public void removeHeader(String name) {
		nativeMessage.removeHeader(name);
	}

	public void clearHeaders() {
		nativeMessage.clearHeaders();
	}

	public boolean isChunked() {
		return nativeMessage.isChunked();
	}

	public void setChunked(boolean chunked) {
		nativeMessage.setChunked(chunked);
	}
	
}
