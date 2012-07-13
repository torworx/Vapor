package evymind.vapor.core.event.handling.disruptor;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import evyframework.common.Assert;

import evymind.vapor.core.QueueFullException;
import evymind.vapor.core.event.handling.EventBus;
import evymind.vapor.core.event.handling.EventListener;
import evymind.vapor.core.event.handling.EventListenerProxy;

public class DisruptorEventBus implements EventBus {

	private static final Logger log = LoggerFactory.getLogger(DisruptorEventBus.class);
	private static final ThreadGroup DISRUPTOR_THREAD_GROUP = new ThreadGroup("DisruptorEventBus");

	private final Set<EventListener> listeners = new CopyOnWriteArraySet<EventListener>();
	private final Disruptor<EventHandlingEntry> disruptor;
	// private final EventHandlerInvoker eventHandlerInvoker;
	private final ExecutorService executorService;
	private volatile boolean started = true;
	private volatile boolean disruptorShutDown = false;
	private final long coolingDownPeriod;

	public DisruptorEventBus() {
		this(new DisruptorConfiguration());
	}

	@SuppressWarnings("unchecked")
	public DisruptorEventBus(DisruptorConfiguration configuration) {
		Executor executor = configuration.getExecutor();
		if (executor == null) {
			executorService = Executors.newCachedThreadPool(new SimpleThreadFactory(DISRUPTOR_THREAD_GROUP));
			executor = executorService;
		} else {
			executorService = null;
		}
		disruptor = new Disruptor<EventHandlingEntry>(new EventHandlingEntry.Factory(), executor,
				configuration.getClaimStrategy(), configuration.getWaitStrategy());
		disruptor.handleExceptionsWith(new ExceptionHandler() {
			@Override
			public void handleEventException(Throwable ex, long sequence, Object event) {
				log.error("Exception occurred while processing {}.", event.toString(), ex);
			}

			@Override
			public void handleOnStartException(Throwable ex) {
				log.error("Failed to start the DisruptorEventBus.", ex);
				disruptor.shutdown();
			}

			@Override
			public void handleOnShutdownException(Throwable ex) {
				log.error("Error while shutting down the DisruptorEventBus", ex);
			}
		});
		disruptor.handleEventsWith(new EventHandlerInvoker(listeners));
		coolingDownPeriod = configuration.getCoolingDownPeriod();
		disruptor.start();
	}

	@Override
	public void publish(Object... events) throws QueueFullException {
        Assert.isTrue(!disruptorShutDown, "Disruptor has been shut down. Cannot dispatch or redispatch events");
        RingBuffer<EventHandlingEntry> ringBuffer = disruptor.getRingBuffer();
        try {
            long sequence = ringBuffer.tryNext(16);
            EventHandlingEntry event = ringBuffer.get(sequence);
            event.reset(events);
            ringBuffer.publish(sequence);
		} catch (InsufficientCapacityException e) {
			throw new QueueFullException(e);
		}
	}

	@Override
	public void subscribe(EventListener eventListener) {
		Class<?> listenerType = getActualListenerType(eventListener);
		if (listeners.add(eventListener)) {
			log.debug("EventListener [{}] subscribed successfully", listenerType.getSimpleName());
		} else {
			log.info("EventListener [{}] not added. It was already subscribed", listenerType.getSimpleName());
		}
	}

	@Override
	public void unsubscribe(EventListener eventListener) {
		Class<?> listenerType = getActualListenerType(eventListener);
		if (listeners.remove(eventListener)) {
			log.debug("EventListener {} unsubscribed successfully", listenerType.getSimpleName());
		} else {
			log.info("EventListener {} not removed. It was already unsubscribed", listenerType.getSimpleName());
		}
	}

	@Override
	public void unsubscribeAll() {
		listeners.clear();
		log.debug("Unsubscribe all EventListeners successfully");
	}

	private Class<?> getActualListenerType(EventListener eventListener) {
		Class<?> listenerType;
		if (eventListener instanceof EventListenerProxy) {
			listenerType = ((EventListenerProxy) eventListener).getTargetType();
		} else {
			listenerType = eventListener.getClass();
		}
		return listenerType;
	}

    /**
     * Shuts down the event bus. It no longer accepts new events, and finishes processing events that have
     * already been published. This method will not shut down any executor that has been provided as part of the
     * Configuration.
     */
    public void stop() {
        if (!started) {
            return;
        }
        started = false;
        long lastChangeDetected = System.currentTimeMillis();
        long lastKnownCursor = disruptor.getRingBuffer().getCursor();
        while (System.currentTimeMillis() - lastChangeDetected < coolingDownPeriod && !Thread.interrupted()) {
            if (disruptor.getRingBuffer().getCursor() != lastKnownCursor) {
                lastChangeDetected = System.currentTimeMillis();
                lastKnownCursor = disruptor.getRingBuffer().getCursor();
            }
        }
        disruptorShutDown = true;
        disruptor.shutdown();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
	private static class SimpleThreadFactory implements ThreadFactory {

		private final ThreadGroup group;

		public SimpleThreadFactory(ThreadGroup group) {
			this.group = group;
		}

		@Override
		public Thread newThread(Runnable r) {
			return new Thread(group, r);
		}
	}
}
