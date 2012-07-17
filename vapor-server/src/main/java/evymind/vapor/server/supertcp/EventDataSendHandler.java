package evymind.vapor.server.supertcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.core.QueueFullException;
import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.event.handling.annontation.EventHandler;
import evymind.vapor.core.supertcp.PackageAck;

public class EventDataSendHandler {
	
	private static final Logger log = LoggerFactory.getLogger(EventDataSendHandler.class);
	
	private final BaseSuperTCPConnector connector;
	
	public EventDataSendHandler(BaseSuperTCPConnector connector) {
		this.connector = connector;
	}

	@EventHandler
	protected void asyncHandleEventDataSendEvent(EventDataSendEvent event) {
		doHandleEventDataSendEvent(event, false);
	}
	
	public void handleEventDataSendEvent(EventDataSendEvent event) {
		doHandleEventDataSendEvent(event, true);
	}
	
	private static long sequence = 0;
	public void doHandleEventDataSendEvent(EventDataSendEvent event, boolean sync) {
		SCServerWorker worker = event.getWorker();
		if (!connector.getClientManager().isValid(worker)) {
			return;
		}
		
		long eventSequence = sequence++;
		VaporBuffer data = event.getData().copy();
		log.debug("<ESEQ:{}> Sending EventDataSendEvent with data :{}", eventSequence, data);
		data.markReaderIndex();
		while (true) {
			try {
				PackageAck wak = worker.sendPackage(0, data);
				log.debug("<ESEQ:{}, WAK:{}>Event data has been sent", eventSequence, wak.getAckNo());
				SCServerWorker.waitForAck(wak, connector.getAckWaitTimeout());
	//	        if Supports(fOwner.fEventRepository, IROValidatedSessionsChangesListener, asv) and not IsEqualGUID(fid, EmptyGUID) then
	//	          asv.EventSucceeded(fClientGuid, fid);
				log.debug("<ESEQ:{}, WAK:{}> Success sent EventDataSendEvent with data :{}", new Object[]{eventSequence, wak.getAckNo(), data});
				return;
			} catch (QueueFullException e) {
				if (!sync) {
					connector.handleEventDataSend(event);
					return;
				}
			} catch (Exception e) {
				log.error("<ESEQ:{}> Error send EventDataSendEvent data {} to {}", new Object[]{eventSequence, event.getData(), worker.getClientId()});
				log.error(e.getMessage(), e);
//				if (connector.getEventRepository() != null) {
//					data.resetReaderIndex();
//					log.warn("Fail send EventDataSendEvent and retry with data :{}", data);
//					connector.getEventRepository().publish(data, UuidUtils.EMPTY_UUID, event.getDestination());
//				}
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
