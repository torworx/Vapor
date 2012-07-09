package evymind.vapor.client.supertcp.netty;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import evymind.vapor.client.supertcp.SCClientWorker;
import evymind.vapor.client.supertcp.SuperClient;
import evymind.vapor.core.netty.LoggingHandler;
import evymind.vapor.core.supertcp.netty.NettySupperConnection;

public class NettySuperClient extends NettySupperConnection implements SuperClient {

	private ClientBootstrap bootstrap;

	public NettySuperClient(final SCClientWorker worker) {
		super();
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		// Configure the event pipeline factory.
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = pipeline();
				pipeline.addLast("logger", new LoggingHandler("CLIENT", true));
				pipeline.addLast("handler", new NettySuperClientHandler(NettySuperClient.this, worker));
				return pipeline;
			}
		});
		
		worker.setConnection(this);
	}

	@Override
	public void connect(String host, int port) throws InterruptedException {
		connect(host, port, 10000);
	}

	@Override
	public void connect(String host, int port, int timeoutMillis) throws InterruptedException {
		if (!isConnected()) {
			bootstrap.connect(new InetSocketAddress(host, port)).await(timeoutMillis);
		}
	}

}
