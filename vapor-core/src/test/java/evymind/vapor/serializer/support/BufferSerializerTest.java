package evymind.vapor.serializer.support;

import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import evymind.vapor.core.VaporBuffer;
import evymind.vapor.core.buffer.VaporBuffers;
import evymind.vapor.core.serializer.bin.BufferSerializer;


public class BufferSerializerTest {
	
	private VaporBuffer buffer;
	private BufferSerializer serializer;
	
	@Before
	public void initialize() {
		buffer = VaporBuffers.dynamicBuffer();
		serializer = new BufferSerializer(buffer);
	}
	
	@Test
	public void testWriteReadDate() {
		Date date = new Date();
		serializer.write("", date);
		Date date2 = serializer.read("", Date.class);
		Assert.assertEquals(date, date2);
	}
	
	@Test
	public void testWriteReadObject() {
		Foo foo = new Foo("foo", 1, 3.5f);
		foo.setDateValue(new Date());
		serializer.write("", foo);
		Foo foo2 = serializer.read("", Foo.class);
		Assert.assertEquals(foo, foo2);
	}


}
