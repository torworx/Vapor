package evymind.vapor.client.supertcp.netty;

import java.nio.channels.ClosedChannelException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import evymind.vapor.client.supertcp.SCClientWorker;
import evymind.vapor.core.VaporRuntimeException;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.netty.ChannelBufferWrapper;

public class NettySuperClientHandler extends SimpleChannelUpstreamHandler{
	
	private NettySuperClient client;
	private SCClientWorker worker;

	public NettySuperClientHandler(NettySuperClient client, SCClientWorker worker) {
		super();
		this.client = client;
		this.worker = worker;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
//		if (Logs.TCP_DATA_LOG.isDebugEnabled()) {
//			ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
//			Logs.TCP_DATA_LOG.debug("==> Received :{}\n{}", e.getMessage(), HexDump.format(buffer.array()));
//		}
		VaporBuffer buffer = new ChannelBufferWrapper((ChannelBuffer) e.getMessage());
		worker.process(buffer);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		client.setChannel(e.getChannel());
		worker.sendHandshake();
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		worker.disconnected();
		client.setChannel(null);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if (ClosedChannelException.class.isAssignableFrom(e.getCause().getClass())) {
			// no-op
		} else {
			throw new VaporRuntimeException(e.getCause());
		}
	}
	
}
