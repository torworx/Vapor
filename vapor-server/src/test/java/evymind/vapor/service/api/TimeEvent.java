package evymind.vapor.service.api;

import java.util.Date;

public class TimeEvent {
	
	private Date time;

	public TimeEvent() {
	}

	public TimeEvent(Date time) {
		this.time = time;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}
	
}
