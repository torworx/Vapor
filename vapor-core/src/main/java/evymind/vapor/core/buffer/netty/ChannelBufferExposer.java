package evymind.vapor.core.buffer.netty;

import org.jboss.netty.buffer.ChannelBuffer;

import evymind.vapor.core.VaporBuffer;


public interface ChannelBufferExposer extends VaporBuffer {
	
	/**
	 * Returns the underlying Netty's ChannelBuffer
	 * 
	 * @return the underlying Netty's ChannelBuffer
	 */
	ChannelBuffer channelBuffer();
}
