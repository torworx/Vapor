package evymind.vapor.server.supertcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.core.QueueFullException;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.event.handling.annontation.EventHandler;
import evymind.vapor.core.supertcp.PackageAck;
import evymind.vapor.core.utils.UuidUtils;

public class EventDataSendEventHandler {
	
	private static final Logger log = LoggerFactory.getLogger(EventDataSendEventHandler.class);
	
	private final BaseSuperTCPConnector connector;
	
	public EventDataSendEventHandler(BaseSuperTCPConnector connector) {
		this.connector = connector;
	}

	@EventHandler
	protected void asyncHandleEventDataSendEvent(EventDataSendEvent event) {
		doHandleEventDataSendEvent(event, false);
	}
	
	public void handleEventDataSendEvent(EventDataSendEvent event) {
		doHandleEventDataSendEvent(event, true);
	}
	
	private static long no = 0;
	public void doHandleEventDataSendEvent(EventDataSendEvent event, boolean sync) {
		long eventNo = no++;
		VaporBuffer data = event.getData().copy();
		log.debug("<ENO:{}> Sending EventDataSendEvent with data :{}", eventNo, data);
		data.markReaderIndex();
		while (true) {
			SCServerWorker worker = event.getWorker();
			try {
				if (!connector.getClientManager().isValid(worker)) {
					return;
				}
				PackageAck wak = worker.sendPackage(0, data);
				log.debug("<ENO:{}, WAK:{}>Event data has been sent", eventNo, wak.getAckNo());
				SCServerWorker.waitForAck(wak, connector.getAckWaitTimeout());
	//	        if Supports(fOwner.fEventRepository, IROValidatedSessionsChangesListener, asv) and not IsEqualGUID(fid, EmptyGUID) then
	//	          asv.EventSucceeded(fClientGuid, fid);
				log.debug("<ENO:{}, WAK:{}> Success sent EventDataSendEvent with data :{}", new Object[]{eventNo, wak.getAckNo(), data});
				return;
			} catch (QueueFullException e) {
				if (!sync) {
					connector.handleEventDataSend(event);
					return;
				}
			} catch (Exception e) {
				log.error("<ENO:{}> Error send EventDataSendEvent data {} to {}", new Object[]{eventNo, event.getData(), worker.getClientId()});
				log.error(e.getMessage(), e);
				if (connector.getEventRepository() != null) {
					data.resetReaderIndex();
					log.warn("Fail send EventDataSendEvent and retry with data :{}", data);
					connector.getEventRepository().publish(data, UuidUtils.EMPTY_UUID, event.getDestination());
				}
				return;
	//	        on E: Exception do begin
	//	          if fOwner.fEventRepository <> nil then begin
	//	            if Supports(fOwner.fEventRepository, IROSessionsChangesListener, aref) then
	//	              aref.SessionsChangedNotification(fClientGuid, saRemoveActiveListener, obj);
	//	            if IsEqualGUID(fid, EmptyGUID) or not Supports(fOwner.fEventRepository, IROValidatedSessionsChangesListener) then
	//	              fOwner.fEventRepository.StoreEventData(EmptyGUID, ms, false, false, GUIDToString(fClientGuid));
	//	            exit;
	//	          end;
	//	        end;
			}
		}
		
	}

}
