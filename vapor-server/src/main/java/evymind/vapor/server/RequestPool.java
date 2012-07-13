package evymind.vapor.server;

import java.util.NoSuchElementException;

import evyframework.pool.BasePoolableObjectFactory;
import evyframework.pool.ObjectPool;
import evyframework.pool.impl.GenericObjectPool;
import evyframework.pool.impl.GenericObjectPoolConfig;
import evymind.vapor.core.VaporRuntimeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestPool {
	
	private static final Logger log = LoggerFactory.getLogger(RequestPool.class);
	
	private final ObjectPool<Request> pool;
	
	public RequestPool() {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(100000);
		config.setMaxIdle(10000);
		pool = new GenericObjectPool<Request>(new RequestFactory(), config);
	}

	public Request borrowRequest() {
		try {
			Request request = pool.borrowObject();
			log.debug("Borrowed {}", request);
			return request;
		} catch (NoSuchElementException e) {
			throw new VaporRuntimeException(e);
		} catch (IllegalStateException e) {
			throw new VaporRuntimeException(e);
		} catch (Exception e) {
			throw new VaporRuntimeException(e);
		}
	}
	
	public void returnRequest(Request request) {
		try {
			pool.returnObject(request);
			log.debug("Returned {}", request);
		} catch (Exception e) {
			throw new VaporRuntimeException(e);
		}
	}

	
	public class RequestFactory extends BasePoolableObjectFactory<Request> {

		@Override
		public Request makeObject() throws Exception {
			return new Request();
		}

		@Override
		public void passivateObject(Request request) throws Exception {
			request.recycle();
		}
		
	}
}
