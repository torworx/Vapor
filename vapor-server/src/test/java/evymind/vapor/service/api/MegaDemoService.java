package evymind.vapor.service.api;

import java.util.List;

public interface MegaDemoService {
	
	int sum(int a, int b);
	
	List<String> getList(String a, String b);
	
	String echo(String message);
	
	String getAddressFromRequest();
	
	void setSessionValue(String name, Object value);
	
	Object getSessionValue(String name);
	
	void subscribeTime(int interval);

}
