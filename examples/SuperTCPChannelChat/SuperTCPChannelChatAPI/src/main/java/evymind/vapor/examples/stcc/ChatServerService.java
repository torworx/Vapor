package evymind.vapor.examples.stcc;

public interface ChatServerService {
	
	void talk(String message);
	
	void talkPrivate(String targetNickname, String message);
}
