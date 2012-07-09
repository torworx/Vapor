package evymind.vapor.core.supertcp.netty;

import java.net.InetSocketAddress;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.Assert;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.Buffers;
import evymind.vapor.core.buffer.netty.ChannelBufferExposer;
import evymind.vapor.core.supertcp.SuperConnection;

public class NettySupperConnection implements SuperConnection {
	
	private static final Logger log = LoggerFactory.getLogger(NettySupperConnection.class);

	private Channel channel;

	public NettySupperConnection() {
	}

	public NettySupperConnection(Channel channel) {
		this.channel = channel;
	}

	public Channel getChannel() {
		return this.channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
	
	@Override
	public void disconnect() {
		if (isConnected()) {
			channel.disconnect();
		}
	}

	@Override
	public boolean isConnected() {
		return channel != null && channel.isConnected();
	}

	@Override
	public String getRemoteAddress() {
		// TODO Auto-generated method stub
		return ((InetSocketAddress) getChannel().getRemoteAddress()).getAddress().getHostAddress();
	}

	@Override
	public int getRemotePort() {
		return ((InetSocketAddress) getChannel().getRemoteAddress()).getPort();
	}

	@Override
	public void writeBuffer(VaporBuffer buffer) {
		Assert.notNull(channel, "'channel' has not been setted");
		if (!isConnected()) {
			log.warn("{} is not connected, operation stopped", channel);
			return;
		}
		getChannel().write(((ChannelBufferExposer) buffer).channelBuffer()).awaitUninterruptibly();
//		if (Logs.TCP_DATA_LOG.isDebugEnabled()) {
//			byte[] data = new byte[buffer.writerIndex()];
//			buffer.getBytes(0, data);
//			Logs.TCP_DATA_LOG.debug("<== Sended : {}\n{}", buffer, HexDump.format(data));
//		}
	}

	@Override
	public void writeString(String value) {
		VaporBuffer buffer = Buffers.dynamicBuffer();
		buffer.writeString(value);
		writeBuffer(buffer);
	}

	@Override
	public void writeBytes(byte[] value) {
		VaporBuffer buffer = Buffers.dynamicBuffer();
		buffer.writeBytes(value);
		writeBuffer(buffer);
	}

}
