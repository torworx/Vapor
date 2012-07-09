package evymind.vapor.app.config;

public interface Configurable<T> {
	
	void configure(T context);

}
