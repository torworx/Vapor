package evymind.vapor.examples.stcc.impl;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

public class Chats {
	
	public static final Map<String, UUID> USERS = Maps.newConcurrentMap();
}
