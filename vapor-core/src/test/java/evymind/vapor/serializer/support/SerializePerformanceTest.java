package evymind.vapor.serializer.support;

import java.util.Date;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SerializePerformanceTest extends AbstractProtobuffWrapperTest {
	
	private static String STRING_VALUE;
	
	static {
		String v = SerializePerformanceTest.class.getName();
		for (int i = 0; i < 100; i++) {
			STRING_VALUE += v;
		}
	}

	@Rule
	public ContiPerfRule i = new ContiPerfRule();
	
	@Before
	public void intialize() throws InterruptedException {
		Thread.sleep(10);
	}
	
	@Test
	@PerfTest(invocations = 10 * 10000)
	public void testSerializeString() {
		serialize(STRING_VALUE);
	}

	@Test
	@PerfTest(invocations = 10 * 10000)
	public void testSerializeAndDeserializeObject() {
		Foo value = new Foo("value", 1, 3.5f);
		value.setDateValue(new Date());
		value.getStrings().add("Hello World!");

		byte[] data = serialize(value);

		Assert.assertEquals(value, deserialize(data).getTarget());
	}
	
	@Test
	@PerfTest(invocations = 10 * 10000)
	public void testSerializeAndDeserializeString() {
		byte[] data = serialize(STRING_VALUE);
		Assert.assertEquals(STRING_VALUE, deserialize(data).getTarget());
	}

}
