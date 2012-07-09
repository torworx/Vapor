package evymind.vapor.server;

import java.io.IOException;

import evymind.vapor.core.utils.component.Lifecycle;
import evymind.vapor.server.eventrepository.EventRepository;

public interface Connector extends Lifecycle {

	/* ------------------------------------------------------------ */
	/**
	 * @return the name of the connector. Defaults to the HostName:port
	 */
	String getName();

	/* ------------------------------------------------------------ */
	/**
	 * Opens the connector
	 * 
	 * @throws IOException
	 */
	void open() throws IOException;

	/* ------------------------------------------------------------ */
	void close() throws IOException;
	
	boolean isOpen();

	/* ------------------------------------------------------------ */
	void setServer(Server server);

	/* ------------------------------------------------------------ */
	Server getServer();
	
	String getHost();
	
	void setHost(String host);

	/* ------------------------------------------------------------ */
	/**
	 * @param port
	 *            The port fto listen of for connections or 0 if any available port may be used.
	 */
	void setPort(int port);

	/* ------------------------------------------------------------ */
	/**
	 * @return The configured port for the connector or 0 if any available port may be used.
	 */
	int getPort();
	
	EventRepository getEventRepository();
}
