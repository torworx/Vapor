package evymind.vapor.core.message.envelope;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

public class MessageEnvelopes implements Map<String, MessageEnvelope> {
	
	private final Map<String, MessageEnvelope> envelopes;
	
	public MessageEnvelopes() {
		this(Maps.newHashMap());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MessageEnvelopes(Map envelopes) {
		this.envelopes = envelopes;
	}

	public void addEnvelope(MessageEnvelope envelope) {
		envelopes.put(envelope.getEnvelopeMarker(), envelope);
	}
	
	public int size() {
		return envelopes.size();
	}

	public boolean isEmpty() {
		return envelopes.isEmpty();
	}

	public boolean containsKey(Object key) {
		return envelopes.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return envelopes.containsValue(value);
	}

	public MessageEnvelope get(Object key) {
		return envelopes.get(key);
	}

	public MessageEnvelope put(String key, MessageEnvelope value) {
		return envelopes.put(key, value);
	}

	public MessageEnvelope remove(Object key) {
		return envelopes.remove(key);
	}

	public void putAll(Map<? extends String, ? extends MessageEnvelope> m) {
		envelopes.putAll(m);
	}

	public void clear() {
		envelopes.clear();
	}

	public Set<String> keySet() {
		return envelopes.keySet();
	}

	public Collection<MessageEnvelope> values() {
		return envelopes.values();
	}

	public Set<java.util.Map.Entry<String, MessageEnvelope>> entrySet() {
		return envelopes.entrySet();
	}

	public boolean equals(Object o) {
		return envelopes.equals(o);
	}

	public int hashCode() {
		return envelopes.hashCode();
	}
	
	
	

}
