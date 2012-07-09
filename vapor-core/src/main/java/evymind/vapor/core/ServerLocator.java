package evymind.vapor.core;

import java.util.HashSet;
import java.util.Set;

import evyframework.common.StringUtils;

public class ServerLocator {

	private String name;
	private boolean disableOnFailure;
	private boolean enabled;
	private String host;
	private int port;
	private String httpDispatcher;
	private boolean loadBalancingServer;
	private Set<ProbingOption> probingOptions = new HashSet<ProbingOption>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDisableOnFailure() {
		return disableOnFailure;
	}

	public void setDisableOnFailure(boolean disableOnFailure) {
		this.disableOnFailure = disableOnFailure;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHttpDispatcher() {
		return httpDispatcher;
	}

	public void setHttpDispatcher(String httpDispatcher) {
		this.httpDispatcher = httpDispatcher;
	}

	public boolean isLoadBalancingServer() {
		return loadBalancingServer;
	}

	public void setLoadBalancingServer(boolean loadBalancingServer) {
		this.loadBalancingServer = loadBalancingServer;
	}

	public Set<ProbingOption> getProbingOptions() {
		return probingOptions;
	}

	public void setProbingOptions(ProbingOption... options) {
		this.probingOptions.clear();
		for (ProbingOption option : options) {
			this.probingOptions.add(option);
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (!StringUtils.hasText(host)) {
			sb.append("!!Empty!!");
		} else {
			sb.append(host);
		}
		
		if (port > 0) {
			sb.append(":").append(port);
		}
		
		if (!enabled) {
			sb.insert(0, "(Disabled)");
		}
		
		sb.append(" ");
		if (loadBalancingServer) {
			sb.append("LBS");
		} else {
			sb.append("non-LBS");
		}
		
		sb.append(", ");
		if (disableOnFailure) {
			sb.append("Disable On Failure");
		}
		
		sb.append(", ")
			.append("[")
			.append(StringUtils.collectionToCommaDelimitedString(probingOptions))
			.append("]");
		
		return sb.toString();
	}

}
