package evymind.vapor.client;

import evymind.vapor.core.Message;
import evymind.vapor.core.TransportChannel;

public class DefaultServiceProxyInvoker implements ServiceProxyInvoker {

	private final Message message;
	private final TransportChannel transportChannel;

	public DefaultServiceProxyInvoker(Message message, TransportChannel transportChannel) {
		this.message = message;
		this.transportChannel = transportChannel;
	}

	@Override
	public void invoke(String interfaceName, String messageName) {
		invoke(interfaceName, messageName, null, null);
	}

	@Override
	public void invoke(String interfaceName, String messageName, Parameters parameters) {
		invoke(interfaceName, messageName, null, parameters);
	}

	@Override
	public <T> T invoke(String interfaceName, String messageName, Class<T> returnType) {
		// TODO Auto-generated method stub
		return invoke(interfaceName, messageName, returnType, null);
	}

	@Override
	public <T> T invoke(String interfaceName, String messageName, Class<T> returnType, Parameters parameters) {
		message.initializeRequestMessage(transportChannel, "", interfaceName, messageName);
		if (parameters != null) {
			for (Parameter parameter : parameters) {
				message.write(parameter.getName(), parameter.getValue(), parameter.getType());
			}
		}
		message.finalizeMessage();
		transportChannel.dispatch(message);
		if (returnType != null && !Void.TYPE.equals(returnType)) {
			return message.read("result", returnType);
		}
		return null;
	}

}
