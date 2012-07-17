package evymind.vapor.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import evymind.vapor.client.event.BeginProbeServerEvent;
import evymind.vapor.client.event.BeginProbeServersEvent;
import evymind.vapor.client.event.EndProbeServerEvent;
import evymind.vapor.client.event.EndProbeServersEvent;
import evymind.vapor.core.DispatchOption;
import evymind.vapor.core.Message;
import evymind.vapor.core.ProbingOption;
import evymind.vapor.core.VaporRuntimeException;
import evymind.vapor.core.ServerLocator;
import evymind.vapor.core.ServerLocators;
import evymind.vapor.core.Transport;
import evymind.vapor.core.TransportChannel;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.VaporBuffers;
import evymind.vapor.core.event.component.EventMulticasterWrapper;
import evymind.vapor.core.message.Messages;

public abstract class AbstractTransportChannel extends EventMulticasterWrapper implements Transport, TransportChannel, Cloneable {

	private int probeFrequency = 60000; // Every minute
	private int loadBalancerLocatorIndex = -1;
	private int faultToleranceLocatorIndex = -1;
	private ServerLocators serverLocators = new ServerLocators();
	private boolean busy;
	private ServerLocator currentLocator;
	private Set<DispatchOption> dispatchOptions = new HashSet<DispatchOption>();
	private boolean threadSafe;
	private boolean probeServers;
	private Timer probeTimer = new Timer();
	
	private TransportChannel probeChannel;
	
	@Override
	public void setServerLocator(ServerLocator serverLocator) {
		// TODO: fire set server locator event
		doSetServerLocator(serverLocator);
	}
	
	protected abstract void doSetServerLocator(ServerLocator serverLocator);

	@Override
	public boolean probe(ServerLocator serverLocator) {
		boolean result = false;
		if (probeChannel == null) {
			probeChannel = (TransportChannel) clone();
		}
		resetProbeChannel(probeChannel);
		probeChannel.setServerLocator(serverLocator);
		
		VaporBuffer request = VaporBuffers.dynamicBuffer(Messages.PROBE_REQUEST_ID);
		VaporBuffer response = VaporBuffers.dynamicBuffer();
		
		try {
			multicastEvent(new BeginProbeServerEvent(this, serverLocator));
			
			try {
				probeChannel.beforeDispatch(null);
				probeChannel.dispatch(request, response);
				
				byte[] data = new byte[response.readableBytes()];
				response.readBytes(data);
				result = Arrays.equals(Messages.PROBE_RESPONSE_ID, data);
				
				if (!result) {
					throw new RuntimeException();
				}
				
				if (serverLocator.getProbingOptions().contains(ProbingOption.ENABLED_IF_PROBE_SUCCEEDED)) {
					serverLocator.setEnabled(true);
				} 
				
				return true;
			} catch (Exception e) {
				if (serverLocator.getProbingOptions().contains(ProbingOption.DISABLE_IF_PROBE_FAILED)) {
					serverLocator.setEnabled(false);
				}
			}
			
		} finally{
			multicastEvent(new EndProbeServerEvent(this, serverLocator, !result));
		}
		return false;
	}
	
	protected void resetProbeChannel(TransportChannel channel) {
		//
	}

	@Override
	public void probeAll() {
		if (serverLocators.isEmpty()) {
			return;
		}
		
		multicastEvent(new BeginProbeServersEvent(this));
		
		int probedCount = 0, enabledCount = 0, disabledCount = 0;
		for (ServerLocator locator : serverLocators) {
			if (locator.getProbingOptions().isEmpty()) {
				continue;
			}
			probedCount++;
			if (locator.isEnabled() && locator.getProbingOptions().contains(ProbingOption.PROBE_WHEN_ENABLED)) {
				if (!probe(locator)) {
					disabledCount++;
				}
			} else if (!locator.isEnabled() && locator.getProbingOptions().contains(ProbingOption.PROBE_WHEN_DISABLED)) {
				if (probe(locator)) {
					enabledCount++;
				}
			}
		}
		
		multicastEvent(new EndProbeServersEvent(this, probedCount, enabledCount, disabledCount));
	}

	@Override
	public void dispatch(VaporBuffer request, VaporBuffer response) {
		if (!threadSafe) {
			if (busy) {
				throw new ChannelBusyException("Channel is busy. Try again later.");
			}
			busy = true;
		}
		
		prepareServerLocator();
		
		try {
			while (true) {
				try {
					
					//request.resetReaderIndex();
					doDispatch(request, response);
					return;
				} catch (Exception e) {
					int i = changeServerLocator(faultToleranceLocatorIndex);
					if (i < 0) {
						if (e instanceof RuntimeException) {
							throw (RuntimeException) e;
						} else {
							throw new VaporRuntimeException(e);
						}
					}
					faultToleranceLocatorIndex = i;
				}
			}
		} finally {
			if (!threadSafe) {
				busy = false;
			}
		}

	}
	
	protected void prepareServerLocator() {
		if (dispatchOptions.contains(DispatchOption.LOAD_BALANCED)) {
			loadBalancerLocatorIndex = serverLocators.findNextLocator(loadBalancerLocatorIndex, true);
			if (loadBalancerLocatorIndex >= 0) {
				ServerLocator locator = serverLocators.get(loadBalancerLocatorIndex);
				setServerLocator(locator);
				setCurrentServerLocator(locator);
				
				faultToleranceLocatorIndex = loadBalancerLocatorIndex;
			}
		}
	}
	
	protected abstract void doDispatch(VaporBuffer request, VaporBuffer response);
	
	protected int changeServerLocator(int index) {
		if (dispatchOptions.contains(DispatchOption.FAULT_TOLERANT)) {
			if ((currentLocator != null) && (currentLocator.isDisableOnFailure())) {
				currentLocator.setEnabled(false);
			}
			int i = serverLocators.findNextLocator(index, false);
			if (i >= 0) {
				ServerLocator locator = serverLocators.get(i);
				setServerLocator(locator);
				setCurrentServerLocator(locator);
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public void dispatch(Message message) {
		VaporBuffer request = VaporBuffers.dynamicBuffer();
		beforeDispatch(message);
		message.writeToBuffer(request);
		while (true) {
			try {
				VaporBuffer response = VaporBuffers.dynamicBuffer();
				dispatch(request, response);
				message.initializeRead(this);
				message.readFromBuffer(response);
				return;
			} catch (RuntimeException e) {
				// TODO: handle session not found exception -- fire need login event
				throw e;
			}
		}
	}

	@Override
	public void beforeDispatch(Message message) {
	}
	
	public abstract AbstractTransportChannel clone();

	public int getProbeFrequency() {
		return probeFrequency;
	}

	public void setProbeFrequency(int probeFrequency) {
		this.probeFrequency = probeFrequency;
	}

	public int getLoadBalancerLocatorIndex() {
		return loadBalancerLocatorIndex;
	}

	public void setLoadBalancerLocatorIndex(int loadBalancerLocatorIndex) {
		this.loadBalancerLocatorIndex = loadBalancerLocatorIndex;
	}

	public int getFaultToleranceLocatorIndex() {
		return faultToleranceLocatorIndex;
	}

	public void setFaultToleranceLocatorIndex(int faultToleranceLocatorIndex) {
		this.faultToleranceLocatorIndex = faultToleranceLocatorIndex;
	}

	public ServerLocators getServerLocators() {
		return serverLocators;
	}

	public void setServerLocators(ServerLocators serverLocators) {
		this.serverLocators = serverLocators;
	}

	public ServerLocator getCurrentServerLocator() {
		return currentLocator;
	}

	public void setCurrentServerLocator(ServerLocator currentLocator) {
		this.currentLocator = currentLocator;
	}

	public Set<DispatchOption> getDispatchOptions() {
		return dispatchOptions;
	}

	public void setDispatchOptions(Collection<DispatchOption> dispatchOptions) {
		this.dispatchOptions.clear();
		for (DispatchOption option : dispatchOptions) {
			this.dispatchOptions.add(option);
		}
	}

	public boolean isThreadSafe() {
		return threadSafe;
	}

	public void setThreadSafe(boolean threadSafe) {
		this.threadSafe = threadSafe;
	}

	public boolean isBusy() {
		return busy;
	}

	public boolean isProbeServers() {
		return probeServers;
	}

	public void setProbeServers(boolean probeServers) {
		if (this.probeServers != probeServers) {
			this.probeServers = probeServers;
			if (this.probeServers) {
				probeTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						AbstractTransportChannel.this.probeAll();
					}
				}, probeFrequency, probeFrequency);
			} else {
				probeTimer.cancel();
			}
		}
	}

}
