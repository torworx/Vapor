package evymind.vapor.server;

import java.util.NoSuchElementException;

import evyframework.pool.BasePoolableObjectFactory;
import evyframework.pool.ObjectPool;
import evyframework.pool.impl.GenericObjectPool;
import evyframework.pool.impl.GenericObjectPoolConfig;
import evymind.vapor.core.RemotingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponsePool {
	
	private static final Logger log = LoggerFactory.getLogger(ResponsePool.class);

	private final ObjectPool<Response> pool;
	
	public ResponsePool() {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(10000);
		config.setMaxIdle(1000);
		pool = new GenericObjectPool<Response>(new ResponseFactory(), config);
	}
	
	public Response borrowResponse() {
		try {
			Response response = pool.borrowObject();
			log.debug("Borrowed {}", response);
			return response;
		} catch (NoSuchElementException e) {
			throw new RemotingException(e);
		} catch (IllegalStateException e) {
			throw new RemotingException(e);
		} catch (Exception e) {
			throw new RemotingException(e);
		}
	}
	
	public void returnResponse(Response response) {
		try {
			pool.returnObject(response);
			log.debug("Returned {}", response);
		} catch (Exception e) {
			throw new RemotingException(e);
		}
	}

	
	public class ResponseFactory extends BasePoolableObjectFactory<Response> {

		@Override
		public Response makeObject() throws Exception {
			return new Response();
		}

		@Override
		public void passivateObject(Response response) throws Exception {
			response.reset();
		}
		
	}

}
