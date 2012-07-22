package evymind.vapor.examples.stcc;

import java.util.Collection;

public class UserLogoutEvent extends AbstractPresenceEvent {

	public UserLogoutEvent(Collection<String> nicknames) {
		super(nicknames);
	}

	public UserLogoutEvent(String... nickname) {
		super(nickname);
	}

}
