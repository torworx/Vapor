package evymind.vapor.server;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import evymind.vapor.core.utils.UuidUtils;

public class ClientList {

	private final List<UUID> clientIds = Lists.newArrayList();
	
	public static ClientList newClientList() {
		return new ClientList();
	}

	public List<UUID> getClientIds() {
		return Collections.unmodifiableList(clientIds);
	}

	public ClientList include(Collection<UUID> clientIds) {
		this.clientIds.addAll(clientIds);
		return this;
	}

	public ClientList include(UUID... clientIds) {
		for (UUID clientId : clientIds) {
			if (!UuidUtils.isEmpty(clientId)) {
				if (!this.clientIds.contains(clientId)) {
					this.clientIds.add(clientId);
				}
			}
		}
		return this;
	}

	public ClientList exclude(UUID... clientIds) {
		for (UUID clientId : clientIds) {
			if (!UuidUtils.isEmpty(clientId)) {
				this.clientIds.remove(clientId);
			}
		}
		return this;
	}

}
