package evymind.vapor.core.utils.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregateLifecycle extends AbstractLifecycle implements Destroyable, Dumpable {

	private static final Logger log = LoggerFactory.getLogger(AggregateLifecycle.class);
	private final List<Bean> beans = new CopyOnWriteArrayList<Bean>();
	private boolean started = false;

	private class Bean {

		final Object bean;
		volatile boolean managed = true;
		
		Bean(Object bean) {
			this.bean = bean;
		}

		public String toString() {
			return "{" + bean + "," + managed + "}";
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * Start the managed lifecycle beans in the order they were added.
	 * 
	 * @see evymind.vapor.core.utils.component.AbstractLifecycle#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		for (Bean b : this.beans) {
			if (b.managed && b.bean instanceof Lifecycle) {
				Lifecycle l = (Lifecycle) b.bean;
				if (!l.isRunning())
					l.start();
			}
		}
		// indicate that we are started, so that addBean will start other beans
		// added.
		this.started = true;
		super.doStart();
	}

	/* ------------------------------------------------------------ */
	/**
	 * Stop the joined lifecycle beans in the reverse order they were added.
	 * 
	 * @see evymind.vapor.core.utils.component.AbstractLifecycle#doStart()
	 */
	@Override
	protected void doStop() throws Exception {
		this.started = false;
		super.doStop();
		List<Bean> reverse = new ArrayList<Bean>(this.beans);
		Collections.reverse(reverse);
		for (Bean b : reverse) {
			if (b.managed && b.bean instanceof Lifecycle) {
				Lifecycle l = (Lifecycle) b.bean;
				if (l.isRunning())
					l.stop();
			}
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * Destroy the joined Destroyable beans in the reverse order they were
	 * added.
	 * 
	 * @see evymind.vapor.core.utils.component.Destroyable#destroy()
	 */
	public void destroy() {
		List<Bean> reverse = new ArrayList<Bean>(this.beans);
		Collections.reverse(reverse);
		for (Bean b : reverse) {
			if (b.bean instanceof Destroyable && b.managed) {
				Destroyable d = (Destroyable) b.bean;
				d.destroy();
			}
		}
		this.beans.clear();
	}

	/* ------------------------------------------------------------ */
	/**
	 * Is the bean contained in the aggregate.
	 * 
	 * @param bean
	 * @return True if the aggregate contains the bean
	 */
	public boolean contains(Object bean) {
		for (Bean b : this.beans)
			if (b.bean == bean)
				return true;
		return false;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Is the bean joined to the aggregate.
	 * 
	 * @param bean
	 * @return True if the aggregate contains the bean and it is joined
	 */
	public boolean isManaged(Object bean) {
		for (Bean b : this.beans)
			if (b.bean == bean)
				return b.managed;
		return false;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Add an associated bean. If the bean is a {@link Lifecycle}, then it will
	 * be managed if it is not already started and umanaged if it is already
	 * started. The {@link #addBean(Object, boolean)} method should be used if
	 * this is not correct, or the {@link #manage(Object)} and
	 * {@link #unmanage(Object)} methods may be used after an add to change the
	 * status.
	 * 
	 * @param o
	 *            the bean object to add
	 * @return true if the bean was added or false if it has already been added.
	 */
	public boolean addBean(Object o) {
		// beans are joined unless they are started lifecycles
		return addBean(o, !((o instanceof Lifecycle) && ((Lifecycle) o).isStarted()));
	}

	/* ------------------------------------------------------------ */
	/**
	 * Add an associated lifecycle.
	 * 
	 * @param o
	 *            The lifecycle to add
	 * @param managed
	 *            True if the Lifecycle is to be joined, otherwise it will be
	 *            disjoint.
	 * @return true if bean was added, false if already present.
	 */
	public boolean addBean(Object o, boolean managed) {
		if (contains(o))
			return false;

		Bean b = new Bean(o);
		b.managed = managed;
		this.beans.add(b);

		if (o instanceof Lifecycle) {
			Lifecycle l = (Lifecycle) o;

			// Start the bean if we are started
			if (managed && this.started) {
				try {
					l.start();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return true;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Manage a bean by this aggregate, so that it is started/stopped/destroyed
	 * with the aggregate lifecycle.
	 * 
	 * @param bean
	 *            The bean to manage (must already have been added).
	 */
	public void manage(Object bean) {
		for (Bean b : this.beans) {
			if (b.bean == bean) {
				b.managed = true;
				return;
			}
		}
		throw new IllegalArgumentException();
	}

	/* ------------------------------------------------------------ */
	/**
	 * Unmanage a bean by this aggregate, so that it is not
	 * started/stopped/destroyed with the aggregate lifecycle.
	 * 
	 * @param bean
	 *            The bean to manage (must already have been added).
	 */
	public void unmanage(Object bean) {
		for (Bean b : this.beans) {
			if (b.bean == bean) {
				b.managed = false;
				return;
			}
		}
		throw new IllegalArgumentException();
	}
	
	public void setBeans(Collection<Object> beans) {
		removeBeans();
		if (beans != null) {
			for (Object bean : beans) {
				addBean(bean);
			}
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * Get dependent beans
	 * 
	 * @return List of beans.
	 */
	public Collection<Object> getBeans() {
		return getBeans(Object.class);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Get dependent beans of a specific class
	 * 
	 * @see #addBean(Object)
	 * @param clazz
	 * @return List of beans.
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getBeans(Class<T> clazz) {
		List<T> beans = new ArrayList<T>();
		for (Bean b : this.beans) {
			if (clazz.isInstance(b.bean))
				beans.add((T) (b.bean));
		}
		return beans;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Get dependent beans of a specific class. If more than one bean of the
	 * type exist, the first is returned.
	 * 
	 * @see #addBean(Object)
	 * @param clazz
	 * @return bean or null
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBean(Class<T> clazz) {
		for (Bean b : this.beans) {
			if (clazz.isInstance(b.bean))
				return (T) b.bean;
		}

		return null;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Remove all associated bean.
	 */
	public void removeBeans() {
		this.beans.clear();
	}

	/* ------------------------------------------------------------ */
	/**
	 * Remove an associated bean.
	 */
	public boolean removeBean(Object o) {
		Iterator<Bean> i = this.beans.iterator();
		while (i.hasNext()) {
			Bean b = i.next();
			if (b.bean == o) {
				this.beans.remove(b);
				return true;
			}
		}
		return false;
	}

	/* ------------------------------------------------------------ */
	public void dumpStdErr() {
		try {
			dump(System.err, "");
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}
	}

	/* ------------------------------------------------------------ */
	public String dump() {
		return dump(this);
	}

	/* ------------------------------------------------------------ */
	public static String dump(Dumpable dumpable) {
		StringBuilder b = new StringBuilder();
		try {
			dumpable.dump(b, "");
		} catch (IOException e) {
			log.warn(e.getMessage(), e);
		}
		return b.toString();
	}

	/* ------------------------------------------------------------ */
	public void dump(Appendable out) throws IOException {
		dump(out, "");
	}

	/* ------------------------------------------------------------ */
	protected void dumpThis(Appendable out) throws IOException {
		out.append(String.valueOf(this)).append(" - ").append(getState()).append("\n");
	}

	/* ------------------------------------------------------------ */
	public static void dumpObject(Appendable out, Object o) throws IOException {
		try {
			if (o instanceof Lifecycle)
				out.append(String.valueOf(o)).append(" - ").append((AbstractLifecycle.getState((Lifecycle) o)))
						.append("\n");
			else
				out.append(String.valueOf(o)).append("\n");
		} catch (Throwable th) {
			out.append(" => ").append(th.toString()).append('\n');
		}
	}

	/* ------------------------------------------------------------ */
	public void dump(Appendable out, String indent) throws IOException {
		dumpThis(out);
		int size = this.beans.size();
		if (size == 0)
			return;
		int i = 0;
		for (Bean b : this.beans) {
			i++;

			if (b.managed) {
				out.append(indent).append(" +- ");
				if (b.bean instanceof Dumpable)
					((Dumpable) b.bean).dump(out, indent + (i == size ? "    " : " |  "));
				else
					dumpObject(out, b.bean);
			} else
				dumpObject(out, b.bean);
		}

		if (i != size)
			out.append(indent).append(" |\n");
	}

	/* ------------------------------------------------------------ */
	public static void dump(Appendable out, String indent, Collection<?>... collections) throws IOException {
		if (collections.length == 0)
			return;
		int size = 0;
		for (Collection<?> c : collections)
			size += c.size();
		if (size == 0)
			return;

		int i = 0;
		for (Collection<?> c : collections) {
			for (Object o : c) {
				i++;
				out.append(indent).append(" +- ");

				if (o instanceof Dumpable)
					((Dumpable) o).dump(out, indent + (i == size ? "    " : " |  "));
				else
					dumpObject(out, o);
			}

			if (i != size)
				out.append(indent).append(" |\n");
		}
	}
}
