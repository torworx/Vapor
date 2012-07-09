package evymind.vapor;

public class ObjectHolder<T> {
	
	private T object;
	
	public ObjectHolder() {
	}

	public ObjectHolder(T object) {
		this.object = object;
	}

	public synchronized T get() {
		return object;
	}
	
	public synchronized void set(T object) {
		this.object = object;
	}

}
