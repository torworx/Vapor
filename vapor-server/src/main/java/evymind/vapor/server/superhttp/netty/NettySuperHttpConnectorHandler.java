package evymind.vapor.server.superhttp.netty;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.*;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import evymind.vapor.core.http.netty.HttpRequestWrapper;
import evymind.vapor.core.http.netty.HttpResponseWrapper;
import evymind.vapor.server.superhttp.BaseSuperHttpConnector;

public class NettySuperHttpConnectorHandler extends SimpleChannelUpstreamHandler {

	private BaseSuperHttpConnector server;

	public NettySuperHttpConnectorHandler(BaseSuperHttpConnector server) {
		super();
		this.server = server;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		server.handleRequest(new HttpRequestWrapper(request), new HttpResponseWrapper(response));
		e.getChannel().write(response);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		// TODO Auto-generated method stub
		super.exceptionCaught(ctx, e);
	}
	
}
