/*
 * Copyright (c) 2010-2011. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package evymind.vapor.core.event.handling.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.ClaimStrategy;
import com.lmax.disruptor.MultiThreadedClaimStrategy;
import com.lmax.disruptor.WaitStrategy;

import java.util.concurrent.Executor;

/**
 * Configuration object for the DisruptorEventBus. The DisruptorConfiguration provides access to the options to
 * tweak performance settings. Instances are not thread-safe and should not be altered after they have been used to
 * initialize a DisruptorEventBus.
 */
public class DisruptorConfiguration {

    private ClaimStrategy claimStrategy;
    private WaitStrategy waitStrategy;
    private Executor executor;
    private int poolThreads;
    private long coolingDownPeriod;

    /**
     * Initializes a configuration instance with default settings: ring-buffer size: 8192, blocking wait strategy and
     * multi-threaded claim strategy.
     */
    public DisruptorConfiguration() {
        this.claimStrategy = new MultiThreadedClaimStrategy(8192);
        this.waitStrategy = new BlockingWaitStrategy();
        this.poolThreads = 4;
        this.coolingDownPeriod = 1000;
    }

    /**
     * Returns the ClaimStrategy currently configured.
     *
     * @return the ClaimStrategy currently configured
     */
    public ClaimStrategy getClaimStrategy() {
        return claimStrategy;
    }

    /**
     * Sets the ClaimStrategy (including buffer size) which prescribes how threads get access to provide commands to
     * the EventBus' RingBuffer.
     * <p/>
     * Defaults to a MultiThreadedClaimStrategy with 4096 elements in the RingBuffer.
     *
     * @param claimStrategy The ClaimStrategy to use
     * @return <code>this</code> for method chaining
     *
     * @see com.lmax.disruptor.MultiThreadedClaimStrategy MultiThreadedClaimStrategy
     * @see com.lmax.disruptor.SingleThreadedClaimStrategy SingleThreadedClaimStrategy
     */
    public DisruptorConfiguration setClaimStrategy(ClaimStrategy claimStrategy) { //NOSONAR (setter may hide field)
        this.claimStrategy = claimStrategy;
        return this;
    }

    /**
     * Returns the WaitStrategy currently configured.
     *
     * @return the WaitStrategy currently configured
     */
    public WaitStrategy getWaitStrategy() {
        return waitStrategy;
    }

    /**
     * Sets the <code>WaitStrategy</code>, which used to make dependent threads wait for tasks to be completed. The
     * choice of strategy mainly depends on the number of processors available and the number of tasks other than the
     * <code>DisruptorEventBus</code> being processed.
     * <p/>
     * The <code>BusySpinWaitStrategy</code> provides the best throughput at the lowest latency, but also put a big
     * claim on available CPU resources. The <code>SleepingWaitStrategy</code> yields lower performance, but leaves
     * resources available for other processes to use.
     * <p/>
     * Defaults to the <code>BlockingWaitStrategy</code>.
     *
     * @param waitStrategy The WaitStrategy to use
     * @return <code>this</code> for method chaining
     *
     * @see com.lmax.disruptor.SleepingWaitStrategy SleepingWaitStrategy
     * @see com.lmax.disruptor.BlockingWaitStrategy BlockingWaitStrategy
     * @see com.lmax.disruptor.BusySpinWaitStrategy BusySpinWaitStrategy
     * @see com.lmax.disruptor.YieldingWaitStrategy YieldingWaitStrategy
     */
    public DisruptorConfiguration setWaitStrategy(WaitStrategy waitStrategy) { //NOSONAR (setter may hide field)
        this.waitStrategy = waitStrategy;
        return this;
    }

    /**
     * Returns the Executor providing the processing resources (Threads) for the DisruptorEventBus.
     *
     * @return the Executor providing the processing resources
     */
    public Executor getExecutor() {
        return executor;
    }

    /**
     * Sets the Executor that provides the processing resources (Threads) for the components of the
     * DisruptorEventBus. The provided executor must be capable of providing the required number of threads. Three
     * threads are required immediately at startup and will not be returned until the EventBus is stopped. Additional
     * threads are used to invoke callbacks and start a recovery process in case aggregate state has been corrupted.
     * Failure to do this results in the disruptor hanging at startup, waiting for resources to become available.
     * <p/>
     * Defaults to <code>null</code>, causing the DisruptorEventBus to create the necessary threads itself. In that
     * case, threads are created in the <code>DisruptorEventBus</code> ThreadGroup.
     *
     * @param executor the Executor that provides the processing resources
     * @return <code>this</code> for method chaining
     */
    public DisruptorConfiguration setExecutor(Executor executor) { //NOSONAR (setter may hide field)
        this.executor = executor;
        return this;
    }

    public int getPoolThreads() {
		return poolThreads;
	}

	public void setPoolThreads(int poolThreads) {
		this.poolThreads = poolThreads;
	}

	/**
     * Returns the cooling down period for the shutdown of the DisruptorEventBus, in milliseconds. This is the time
     * in which new commands are no longer accepted, but the DisruptorEventBus may reschedule Commands that may have
     * been executed against a corrupted Aggregate. If no commands have been rescheduled during this period, the
     * disruptor shuts down completely. Otherwise, it wait until no commands were scheduled for processing.
     *
     * @return the cooling down period for the shutdown of the DisruptorEventBus, in milliseconds.
     */
    public long getCoolingDownPeriod() {
        return coolingDownPeriod;
    }

    /**
     * Sets the cooling down period in milliseconds. This is the time in which new commands are no longer accepted, but
     * the DisruptorEventBus may reschedule Commands that may have been executed against a corrupted Aggregate. If no
     * commands have been rescheduled during this period, the disruptor shuts down completely. Otherwise, it wait until
     * no commands were scheduled for processing.
     * <p/>
     * Defaults to 1000 (1 second).
     *
     * @param coolingDownPeriod the cooling down period for the shutdown of the DisruptorEventBus, in milliseconds.
     * @return <code>this</code> for method chaining
     */
    public DisruptorConfiguration setCoolingDownPeriod(long coolingDownPeriod) { //NOSONAR (setter may hide field)
        this.coolingDownPeriod = coolingDownPeriod;
        return this;
    }

}
