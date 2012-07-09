package evymind.vapor.integration;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

public class MultiClientsPerformanceTest {
	
	CountDownLatch latch;
	
	@Test
	public void testSum() throws Exception {
		System.out.println("Begin test");
		int threadCount = 100;
		int reqsPerThread = 1000;
		latch = new CountDownLatch(threadCount);
		Thread[] threads = new Thread[threadCount];
		long start = System.currentTimeMillis();
		for (int i = 0; i < threadCount; i++) {
			Sum sum = new Sum("192.168.3.46", 8095, reqsPerThread);
			threads[i] = new Thread(sum);
			try {
				sum.connect();
			} catch (Exception e) {
				throw e;
			}
			
		}
//		long time = System.currentTimeMillis() - start;
//		System.out.println("--------------------------------------------------------");
//		System.out.println(threadCount * 1000.0 / time + " connects/sec");
//		System.out.println("--------------------------------------------------------");
		
		start = System.currentTimeMillis();
		for (Thread thread : threads) {
			thread.start();
		}
		latch.await();
		long time = System.currentTimeMillis() - start;
		System.out.println("--------------------------------------------------------");
		System.out.println(threadCount * reqsPerThread * 1000.0 / time + " reqs/sec");
		System.out.println("--------------------------------------------------------");
		
	}
	
	public class Sum extends AbstractClient implements Runnable {
		
		private int times;
		
		public Sum(String host, int port, int times) {
			super(host, port);
			this.times = times;
		}

		@Override
		public void run() {
			if (!isConnected()) {
				connect();
			}
//			System.out.println(this + " running...");
			
			for (int i = 0; i < times; i++) {
//				System.out.println("Thread(" + index + ") " + i);
				megaDemoServiceProxy.sum(1, 1);
			}
			disconnect();
			MultiClientsPerformanceTest.this.latch.countDown();
			System.out.println(this + " run complete, latch count=" + MultiClientsPerformanceTest.this.latch.getCount());
		}
		
	}

}
