package evymind.vapor.client.supertcp;

import evymind.vapor.client.supertcp.netty.NettySuperClient;

public class SuperTCPChannel extends BaseSuperTCPChannel {

	@Override
	protected SuperClient createSuperConnection(SCClientWorker worker) {
		return new NettySuperClient(worker);
	}

	@Override
	public SuperTCPChannel clone() {
		SuperTCPChannel answer = new SuperTCPChannel();
		return answer;
	}

}
