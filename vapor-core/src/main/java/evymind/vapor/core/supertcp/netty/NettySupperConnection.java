package evymind.vapor.core.supertcp.netty;

import java.net.InetSocketAddress;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.Assert;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.VaporBuffers;
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
	public synchronized void disconnect() {
		if (isConnected()) {
			channel.disconnect();
		}
	}

	@Override
	public synchronized boolean isConnected() {
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
	public synchronized void writeBuffer(VaporBuffer buffer) {
		writeBuffer(buffer, -1);
	}

	@Override
	public synchronized void writeBuffer(VaporBuffer buffer, int chunkSize) {
		Assert.notNull(channel, "'channel' has not been setted");
		if (!isConnected()) {
			log.warn("{} is not connected, operation stopped", channel);
			return;
		}
		
		final ChannelBuffer channelBuffer = ((ChannelBufferExposer) buffer).channelBuffer();
		getChannel().write(channelBuffer);
//		final ChannelBuffer chunkBuffer = ChannelBuffers.directBuffer(chunkSize);
//		if (channelBuffer.readable()) {
//			chunkBuffer.writeBytes(channelBuffer, Math.min(channelBuffer.readableBytes(), chunkSize));
//			getChannel().write(chunkBuffer).addListener(new ChannelFutureListener() {
//				
//				@Override
//				public void operationComplete(ChannelFuture future) throws Exception {
//					if (future.isSuccess()) {
//						
//					}
//				}
//			});
//		}
	}

	public synchronized void writeString(String value) {
		VaporBuffer buffer = VaporBuffers.dynamicBuffer();
		buffer.writeString(value);
		writeBuffer(buffer);
	}

	public synchronized void writeBytes(byte[] value) {
		VaporBuffer buffer = VaporBuffers.dynamicBuffer();
		buffer.writeBytes(value);
		writeBuffer(buffer);
	}
	
}
