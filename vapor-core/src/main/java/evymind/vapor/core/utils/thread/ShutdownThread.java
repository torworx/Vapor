package evymind.vapor.core.utils.thread;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.core.utils.component.Lifecycle;

/* ------------------------------------------------------------ */
/**
 * ShutdownThread is a shutdown hook thread implemented as singleton that
 * maintains a list of lifecycle instances that are registered with it and
 * provides ability to stop these lifecycles upon shutdown of the Java Virtual
 * Machine
 */
public class ShutdownThread extends Thread {
	
	private static final Logger log = LoggerFactory.getLogger(ShutdownThread.class);
	private static final ShutdownThread instance = new ShutdownThread();

	private boolean hooked;
	private final List<Lifecycle> lifecycles = new CopyOnWriteArrayList<Lifecycle>();

	/* ------------------------------------------------------------ */
	/**
	 * Default constructor for the singleton
	 * 
	 * Registers the instance as shutdown hook with the Java Runtime
	 */
	private ShutdownThread() {
	}

	/* ------------------------------------------------------------ */
	private synchronized void hook() {
		try {
			if (!hooked)
				Runtime.getRuntime().addShutdownHook(this);
			hooked = true;
		} catch (Exception e) {
			log.info("shutdown already commenced");
		}
	}

	/* ------------------------------------------------------------ */
	private synchronized void unhook() {
		try {
			hooked = false;
			Runtime.getRuntime().removeShutdownHook(this);
		} catch (Exception e) {
			log.info("shutdown already commenced");
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * Returns the instance of the singleton
	 * 
	 * @return the singleton instance of the {@link ShutdownThread}
	 */
	public static ShutdownThread getInstance() {
		return instance;
	}

	/* ------------------------------------------------------------ */
	public static synchronized void register(Lifecycle... lifeCycles) {
		instance.lifecycles.addAll(Arrays.asList(lifeCycles));
		if (instance.lifecycles.size() > 0)
			instance.hook();
	}

	/* ------------------------------------------------------------ */
	public static synchronized void register(int index, Lifecycle... lifeCycles) {
		instance.lifecycles.addAll(index, Arrays.asList(lifeCycles));
		if (instance.lifecycles.size() > 0)
			instance.hook();
	}

	/* ------------------------------------------------------------ */
	public static synchronized void unregister(Lifecycle lifeCycle) {
		instance.lifecycles.remove(lifeCycle);
		if (instance.lifecycles.size() == 0)
			instance.unhook();
	}

	/* ------------------------------------------------------------ */
	@Override
	public void run() {
		for (Lifecycle lifeCycle : instance.lifecycles) {
			try {
				lifeCycle.stop();
				log.debug("Stopped {}", lifeCycle);
			} catch (Exception ex) {
				log.debug(ex.getMessage(), ex);
			}
		}
	}
}