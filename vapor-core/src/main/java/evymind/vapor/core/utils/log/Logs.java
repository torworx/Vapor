package evymind.vapor.core.utils.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Logs {
	
	public static final String IGNORED = "IGNORED";
	public static final String EXCEPTION = "EXCEPTION";
	
	public static final Logger TCP_DATA_LOG = LoggerFactory.getLogger("tcp.data");

	private Logs() {};
	
}
