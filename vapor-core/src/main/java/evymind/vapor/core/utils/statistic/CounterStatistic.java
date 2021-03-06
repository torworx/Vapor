package evymind.vapor.core.utils.statistic;

import java.util.concurrent.atomic.AtomicLong;


/**
 * Statistics on a counter value.
 * <p>
 * Keep total, current and maximum values of a counter that can be incremented
 * and decremented. The total refers only to increments.
 * 
 */
public class CounterStatistic {

	protected final AtomicLong _max = new AtomicLong();
	protected final AtomicLong _curr = new AtomicLong();
	protected final AtomicLong _total = new AtomicLong();


	public void reset() {
		reset(0);
	}


	public void reset(final long value) {
		_max.set(value);
		_curr.set(value);
		_total.set(0); // total always set to 0 to properly calculate cumulative
						// total
	}


	/**
	 * @param delta
	 *            the amount to add to the count
	 */
	public void add(final long delta) {
		long value = _curr.addAndGet(delta);
		if (delta > 0)
			_total.addAndGet(delta);
		long oldValue = _max.get();
		while (value > oldValue) {
			if (_max.compareAndSet(oldValue, value))
				break;
			oldValue = _max.get();
		}
	}


	/**
	 * @param delta
	 *            the amount to subtract the count by.
	 */
	public void subtract(final long delta) {
		add(-delta);
	}


	/**
     */
	public void increment() {
		add(1);
	}


	/**
     */
	public void decrement() {
		add(-1);
	}


	/**
	 * @return max value
	 */
	public long getMax() {
		return _max.get();
	}


	/**
	 * @return current value
	 */
	public long getCurrent() {
		return _curr.get();
	}


	/**
	 * @return total value
	 */
	public long getTotal() {
		return _total.get();
	}


	protected void upxdateMax(long value) {
	}
}
