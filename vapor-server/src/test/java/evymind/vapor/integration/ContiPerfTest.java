package evymind.vapor.integration;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ContiPerfTest {
	
	@Rule
	public ContiPerfRule i = new ContiPerfRule();
	
	@Before
	public void setup() {
		System.out.println(Thread.currentThread() + " Before" );
	}
	
	@After
	public void teardown() {
		System.out.println(Thread.currentThread() + " After" );
	}
	
    @Test
    @PerfTest(invocations = 2, threads = 2)
    public void test() throws Exception {
        System.out.println(Thread.currentThread() + " Test");
    }

}
