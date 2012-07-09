package evymind.vapor.server.superhttp.netty;

import static org.jboss.netty.channel.Channels.pipeline;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import evymind.vapor.core.netty.LoggingHandler;
import evymind.vapor.server.superhttp.BaseSuperHttpConnector;

public class NettySuperHttpConnector extends BaseSuperHttpConnector {

	protected ServerBootstrap bootstrap;
	protected Channel channel;

	public NettySuperHttpConnector() {
		super();
		// Configure the server.
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.keepAlive", true);
		bootstrap.setParentHandler(new LoggingHandler("SERVER-PARENT", true));

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				// Create a default pipeline implementation.
				ChannelPipeline pipeline = pipeline();
				pipeline.addLast("logger", new LoggingHandler("SERVER", true));
				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("handler", new NettySuperHttpConnectorHandler(NettySuperHttpConnector.this));
				return pipeline;
			}
		});
	}

	@Override
	public synchronized void open() throws IOException {
		if (!isOpen()) {
			// Bind and start to accept incoming connections.
			this.channel = bootstrap.bind(new InetSocketAddress(getPort()));
		}
	}

	@Override
	public synchronized void close() throws IOException {
		if (isOpen()) {
			channel.close().awaitUninterruptibly();
		}
	}

	@Override
	public boolean isOpen() {
		return channel != null && channel.isOpen();
	}

}
