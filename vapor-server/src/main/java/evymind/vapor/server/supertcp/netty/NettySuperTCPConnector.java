package evymind.vapor.server.supertcp.netty;

import static org.jboss.netty.channel.Channels.pipeline;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import evymind.vapor.core.netty.LoggingHandler;
import evymind.vapor.server.supertcp.BaseSuperTCPConnector;
import evymind.vapor.server.supertcp.ClientManager;
import evymind.vapor.server.supertcp.SCServerClientManager;

public class NettySuperTCPConnector extends BaseSuperTCPConnector {

	protected ServerBootstrap bootstrap;
	protected Channel channel;

	public NettySuperTCPConnector() {
		super();
		// Configure the server.
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		bootstrap.setOption("child.tcpNoDelay", isTcpNoDelay());
		bootstrap.setOption("child.keepAlive", isKeepAlive());
		bootstrap.setParentHandler(new LoggingHandler("SERVER-PARENT", true));

		// Set up the pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = pipeline();
				pipeline.addLast("logger", new LoggingHandler("SERVER", true));
				pipeline.addLast("handler", new NettySuperTCPConnectorHandler(NettySuperTCPConnector.this));
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

	@Override
	protected ClientManager createClientManager() {
		return new SCServerClientManager(this);
	}

}
