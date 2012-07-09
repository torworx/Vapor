package evymind.vapor.server;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evyframework.common.ObjectUtils;
import evymind.vapor.core.Message;
import evymind.vapor.core.Transport;
import evymind.vapor.core.message.MessageException;
import evymind.vapor.core.message.Messages;
import evymind.vapor.core.utils.UuidUtils;
import evymind.vapor.server.handler.HandlerWrapper;

public class ServerInternalHandler extends HandlerWrapper {
	
	private static final Logger log = LoggerFactory.getLogger(ServerInternalHandler.class);

	@Override
	public void handle(Transport transport, Request request, Response response) {
		int requestDataLength = request.getData().readableBytes();
		if (requestDataLength == Messages.PROBE_REQUEST_ID_LENGTH) {
			response.getData().writeBytes(Messages.PROBE_RESPONSE_ID);
			request.markHandled();
		} else {
			Message message = request.getMessage();
			try {
				message.initializeRead(transport);
				message.readFromBuffer(request.getData());
				if (ObjectUtils.nullSafeEquals(UuidUtils.EMPTY_UUID, message.getClientId())) {
					message.setClientId(UUID.randomUUID());
				}
				request.setRequestInterface(message.getInterfaceName());
				request.setRequestMethod(message.getMessageName());
				switch (message.getMessageType()) {
					case REQUEST:
					case RESPONSE:
						super.handle(transport, request, response);
						break;
					default:
						throw new MessageException("Unsupported message type: + " + message.getMessageType());
				}
			} catch (Exception e) {
				log.error(e.getMessage());
				message.initializeExceptionMessage(transport, "", message.getInterfaceName(), message.getMessageName());
				message.writeException(response.getData(), e);
				request.markHandled();
			}
		}
	}
}
