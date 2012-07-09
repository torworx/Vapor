package evymind.vapor.core.utils;

import java.util.UUID;

public class UuidUtils {

	public static final UUID EMPTY_UUID = new UUID(0, 0);
	
	public static boolean isEmpty(UUID uuid) {
		return (uuid == null || EMPTY_UUID.equals(uuid));
	}
}
