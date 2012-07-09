package evymind.vapor.logging;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerTest {
	
	@Test
	public void testLog() {
		Logger log = LoggerFactory.getLogger(getClass());
		log.debug("Test int: {}", 12);
		log.debug("Test stirng: {}", "Hello");
		log.debug(String.format("Test string format stirng: %s", "Hello"));
	}
}
