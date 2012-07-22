package evymind.vapor.examples.stcc;

import java.util.Collection;

public class UserLoginEvent extends AbstractPresenceEvent {

	public UserLoginEvent(Collection<String> nicknames) {
		super(nicknames);
	}

	public UserLoginEvent(String... nickname) {
		super(nickname);
	}

}
