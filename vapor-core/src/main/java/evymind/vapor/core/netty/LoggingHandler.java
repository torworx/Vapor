package evymind.vapor.core.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.logging.InternalLogger;

import evyframework.common.HexDump;
import evymind.vapor.core.utils.log.Logs;

public class LoggingHandler implements ChannelUpstreamHandler, ChannelDownstreamHandler {

	String name;
	boolean hexDump;

	public LoggingHandler(String name, boolean hexDump) {
		this.name = name;
		this.hexDump = hexDump;
	}

	/**
	 * Logs the specified event to the {@link InternalLogger} returned by
	 * {@link #getLogger()}. If hex dump has been enabled for this handler, the
	 * hex dump of the {@link ChannelBuffer} in a {@link MessageEvent} will be
	 * logged together.
	 */
	public void log(ChannelEvent e, boolean upstream) {

		if (Logs.TCP_DATA_LOG.isDebugEnabled()) {
			StringBuffer msg = new StringBuffer(upstream ? "==> [U] " : "<== [D] ");
			msg.append(name).append(" >> ").append(e.toString());
	
			// Append hex dump if necessary.
			if (hexDump && e instanceof MessageEvent) {
				MessageEvent me = (MessageEvent) e;
				if (me.getMessage() instanceof ChannelBuffer) {
					ChannelBuffer buf = (ChannelBuffer) me.getMessage();
					byte[] data;
					if (buf.writableBytes() == 0) {
						data = buf.array();
					} else {
						data = new byte[buf.writerIndex()];
						buf.getBytes(0, data);
					}
					msg.append(" - \n").append(HexDump.format(data));
				}
			}
	
			// Log the message (and exception if available.)
			if (e instanceof ExceptionEvent) {
				Logs.TCP_DATA_LOG.debug(msg.toString(), ((ExceptionEvent) e).getCause());
			} else {
				Logs.TCP_DATA_LOG.debug(msg.toString());
			}
		}

	}

	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		log(e, true);
		ctx.sendUpstream(e);
	}

	public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		log(e, false);
		ctx.sendDownstream(e);
	}
}