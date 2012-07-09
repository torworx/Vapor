package evymind.vapor.serializer.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class ProtostuffWrapperSerializeTest extends AbstractProtobuffWrapperTest {
	
	@Test
	public void testSerializeObject() {
		Foo foo = new Foo("foo", 1, 3.5f);
		foo.setDateValue(new Date());
		foo.getStrings().add("Hello World!");

		byte[] data = serialize(foo);
		
		Assert.assertEquals(foo, deserialize(data).getTarget());
	}
	
	@Test
	public void testSerializeInt() {
		int value = 100;
		byte[] data = serialize(value);
		Assert.assertEquals(value, deserialize(data).getTarget());
	}
	
	@Test
	public void testSerializeList() {
		List<String> value = new ArrayList<String>();
		value.add("A");
		value.add("B");
		
		byte[] data = serialize(value);
		Assert.assertEquals(value, deserialize(data).getTarget());
	}
	
	@Test
	public void testSerializeMap() {
		Map<String, Foo> value = new HashMap<String, Foo>();
		value.put("A", new Foo("A", 1, 1.0f));
		value.put("B", new Foo("B", 2, 2.0f));
		
		byte[] data = serialize(value);
		Assert.assertEquals(value, deserialize(data).getTarget());
	}

}
