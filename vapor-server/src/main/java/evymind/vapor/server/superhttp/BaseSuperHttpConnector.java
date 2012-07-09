package evymind.vapor.server.superhttp;

import java.net.URI;
import java.util.Iterator;

import com.google.common.base.Splitter;

import evymind.vapor.core.http.HttpRequest;
import evymind.vapor.core.http.HttpResponse;
import evymind.vapor.server.AbstractConnector;

public abstract class BaseSuperHttpConnector extends AbstractConnector {
	
	private static final Splitter PATH_SPLITTER = Splitter.on("/").omitEmptyStrings();
	
	private int maxPackageSize = 10 * 1024 * 1024;
	private int connectonTimeout = 5 * 60;
	private boolean autoSubscribeEvent = true;
	
	public BaseSuperHttpConnector() {
		super();
		setPort(8099);
	}

	@SuppressWarnings("unused")
	public void handleRequest(HttpRequest request, HttpResponse response) {
		URI uri = URI.create(request.getUri());
		Iterator<String> paths = PATH_SPLITTER.split(uri.getPath()).iterator();
		String contextPath = "/".concat(paths.hasNext() ? paths.next() : "");
		String subPath = paths.hasNext() ? paths.next() : "";
		
		
	}

	public int getMaxPackageSize() {
		return maxPackageSize;
	}

	public void setMaxPackageSize(int maxPackageSize) {
		this.maxPackageSize = maxPackageSize;
	}

	public int getConnectonTimeout() {
		return connectonTimeout;
	}

	public void setConnectonTimeout(int connectonTimeout) {
		this.connectonTimeout = connectonTimeout;
	}

	public boolean isAutoSubscribeEvent() {
		return autoSubscribeEvent;
	}

	public void setAutoSubscribeEvent(boolean autoSubscribeEvent) {
		this.autoSubscribeEvent = autoSubscribeEvent;
	}

}
