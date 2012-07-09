package evymind.vapor.server.supertcp.netty;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.core.buffer.netty.ChannelBufferWrapper;
import evymind.vapor.core.supertcp.SuperChannelWorker;
import evymind.vapor.core.supertcp.netty.NettySupperConnection;
import evymind.vapor.server.supertcp.BaseSuperTCPConnector;
import evymind.vapor.server.supertcp.ClientManager;
import evymind.vapor.server.supertcp.SCServerWorker;

public class NettySuperTCPConnectorHandler extends SimpleChannelUpstreamHandler {

	private static final Logger log = LoggerFactory.getLogger(NettySuperTCPConnectorHandler.class);

	private ClientManager clientManager;

	public NettySuperTCPConnectorHandler(BaseSuperTCPConnector connector) {
		super();
		this.clientManager = connector.getClientManager();
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		SuperChannelWorker client = clientManager.createClient(new NettySupperConnection(e.getChannel()));
		ctx.setAttachment(client);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		SuperChannelWorker client = (SuperChannelWorker) ctx.getAttachment();
		if (client != null) {
			client.disconnected();
		}
		ctx.setAttachment(null);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
//		if (Logs.TCP_DATA_LOG.isDebugEnabled()) {
//			ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
//			Logs.TCP_DATA_LOG.debug("==> Received :{}\n{}", e.getMessage(), HexDump.format(buffer.array()));
//		}

		SCServerWorker client = (SCServerWorker) ctx.getAttachment();
		client.process(new ChannelBufferWrapper((ChannelBuffer) e.getMessage()));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if (IOException.class.isAssignableFrom(e.getCause().getClass())) {
			// no-op
		} else {
			log.warn("Unexpected exception from upstream.", e.getCause());
		}
		e.getChannel().close();
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		// TODO Auto-generated method stub
		super.channelClosed(ctx, e);
	}

	@Override
	public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e) throws Exception {
		super.writeComplete(ctx, e);
	}

	@Override
	public void childChannelOpen(ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
		// TODO Auto-generated method stub
		super.childChannelOpen(ctx, e);
	}

	@Override
	public void childChannelClosed(ChannelHandlerContext ctx, ChildChannelStateEvent e) throws Exception {
		// TODO Auto-generated method stub
		super.childChannelClosed(ctx, e);
	}

}
