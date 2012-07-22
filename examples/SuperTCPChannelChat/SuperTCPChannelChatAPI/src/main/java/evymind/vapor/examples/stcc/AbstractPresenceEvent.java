package evymind.vapor.examples.stcc;

import java.util.Collection;

public abstract class AbstractPresenceEvent {
	
	private final String[] nicknames;

	public AbstractPresenceEvent(Collection<String> nicknames) {
		this(nicknames != null ? nicknames.toArray(new String[nicknames.size()]) : null);
	}
	
	public AbstractPresenceEvent(String... nicknames) {
		this.nicknames = nicknames != null ? nicknames : new String[0];
	}


	public String[] getNicknames() {
		return nicknames;
	}

}
