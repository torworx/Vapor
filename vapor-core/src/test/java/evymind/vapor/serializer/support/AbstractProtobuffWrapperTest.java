package evymind.vapor.serializer.support;


import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;

import evymind.vapor.core.serializer.bin.Wrapper;

public abstract class AbstractProtobuffWrapperTest {
	
	protected byte[] serialize(Object object) {
		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.MIN_BUFFER_SIZE);
		try {
			return ProtostuffIOUtil.toByteArray(Wrapper.wrap(object), Wrapper.SCHEMA, buffer);
		} finally {
			buffer.clear();
		}
	}
	
	protected Wrapper deserialize(byte[] data) {
		Wrapper wrapper = Wrapper.create();
		ProtostuffIOUtil.mergeFrom(data, wrapper, Wrapper.SCHEMA);
		return wrapper;
	}
}
