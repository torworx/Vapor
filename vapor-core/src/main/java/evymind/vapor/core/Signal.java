package evymind.vapor.core;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Signal {
	
	private final CountDownLatch latch;
	
	public Signal() {
		this(1);
	}
	
	public Signal(int count) {
		latch = new CountDownLatch(count);
	}

	public void signal() {
		latch.countDown();
	}
	
	public void await() throws InterruptedException {
		latch.await();
	}
	
	/**
	 * 
	 * @param timeout the maximum time to wait in milliseconds
	 * @throws InterruptedException
	 */
	public boolean await(long timeout) throws InterruptedException {
		return latch.await(timeout, TimeUnit.MILLISECONDS);
	}

}
