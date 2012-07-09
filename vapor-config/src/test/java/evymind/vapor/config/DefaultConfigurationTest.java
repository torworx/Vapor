package evymind.vapor.config;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Properties;

import org.junit.Test;

import evymind.vapor.server.Server;
import evymind.vapor.server.handler.HandlerCollection;
import evymind.vapor.server.supertcp.netty.NettySuperTCPConnector;

public class DefaultConfigurationTest {
	
	@Test
	public void testLoad() {
		DefaultConfiguration configuration = new DefaultConfiguration();
		Properties properties = new Properties();
		properties.setProperty("ackWaitTimeout", "15000");
		configuration.setProperties(properties);
		configuration.load("classpath:evymind/vapor/config/vapor.ecs");
		
		Collection<Server> servers = configuration.getInstancesOfType(Server.class).values();
		
		// check servers
		assertNotNull(servers);
		assertEquals(1, servers.size());
		Server server = servers.iterator().next();
		
		// check connectors
		assertNotNull(server.getConnectors());
		assertEquals(1, server.getConnectors().length);
		assertTrue(server.getConnectors()[0] instanceof NettySuperTCPConnector);
		NettySuperTCPConnector connector = (NettySuperTCPConnector) server.getConnectors()[0];
		assertEquals(15000, connector.getAckWaitTimeout());
		
		// check handler
		assertNotNull(server.getHandler());
		assertTrue(server.getHandler() instanceof HandlerCollection);
	}

}
