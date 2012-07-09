package evymind.vapor.server.supertcp;

import evymind.vapor.core.supertcp.SuperConnection;

public class SCServerClientManager extends ClientManager {
	
	private BaseSuperTCPConnector connector;

	public SCServerClientManager(BaseSuperTCPConnector connector) {
		super();
		this.connector = connector;
	}
	
	@Override
	protected SCServerWorker doCreateClient(SuperConnection connection) {
		return new SCServerWorker(connector, connection);
	}

}
