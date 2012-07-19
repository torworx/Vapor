package evymind.vapor.server.session;

import java.io.IOException;
import java.util.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evymind.vapor.core.Transport;
import evymind.vapor.server.Request;
import evymind.vapor.server.Response;
import evymind.vapor.server.Server;
import evymind.vapor.server.ServiceException;
import evymind.vapor.server.Session;
import evymind.vapor.server.SessionManager;
import evymind.vapor.server.handler.ScopedHandler;

public class SessionHandler extends ScopedHandler {

	private static final Logger log = LoggerFactory.getLogger(SessionHandler.class.getPackage().getName());

	private SessionManager sessionManager;

	/**
	 * Constructor. Construct a SessionHandler with a HashSessionManager with a standard java.util.Random generator is
	 * created.
	 */
	public SessionHandler() {
		this(new HashSessionManager());
	}

	public SessionHandler(SessionManager sessionManager) {
		setSessionManager(sessionManager);
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		checkNotStarted();
		SessionManager oldSessionManager = sessionManager;

		if (getServer() != null)
			getServer().getContainer().update(this, oldSessionManager, sessionManager, "sessionManager", true);

		// if (sessionManager != null)
		// sessionManager.setSessionHandler(this);

		this.sessionManager = sessionManager;

		// if (oldSessionManager != null)
		// oldSessionManager.setSessionHandler(null);
	}


	@Override
	public void setServer(Server server) {
		Server oldServer = getServer();
		if (oldServer != null && oldServer != server)
			oldServer.getContainer().update(this, sessionManager, null, "sessionManager", true);
		super.setServer(server);
		if (server != null && server != oldServer)
			server.getContainer().update(this, null, sessionManager, "sessionManager", true);
	}


	/*
	 * @see AbstractLifecycle#doStart()
	 */
	@Override
	protected void doStart() throws Exception {
		sessionManager.start();
		super.doStart();
	}


	/*
	 * @see org.eclipse.thread.AbstractLifecycle#doStop()
	 */
	@Override
	protected void doStop() throws Exception {
		// Destroy sessions before destroying services/filters
		sessionManager.stop();
		super.doStop();
	}

	@Override
	public void doScope(Transport transport, Request request, Response response) throws IOException, ServiceException  {
		SessionManager oldSessionManager = null;
		Session oldSession = null;
		Session access = null;
		try {
			oldSessionManager = request.getSessionManager();
			oldSession = request.getSession(false);

			if (oldSessionManager != sessionManager) {
				// new session context
				request.setSessionManager(sessionManager);
				request.setSession(null);
				// checkRequestedSessionId(request, request);
			}

			// access any existing session
			Session session = null;
			if (sessionManager != null) {
				session = request.getSession(false);
				if (session != null) {
					if (session != oldSession) {
						access = session;
						// HttpCookie cookie = sessionManager.access(session, request.isSecure());
						// if (cookie != null) // Handle changed ID or max-age refresh
						// request.getResponse().addCookie(cookie);
					}
				} else {
					// session = request.recoverNewSession(sessionManager);
					// if (session != null)
					// request.setSession(session);
				}
			}

			if (log.isDebugEnabled()) {
				log.debug("sessionManager=" + sessionManager);
				log.debug("session=" + session);
			}

			// start manual inline of nextScope(target,request,request,response);
			if (nextScope != null)
				nextScope.doScope(transport, request, response);
			else if (outerScope != null)
				outerScope.doHandle(transport, request, response);
			else
				doHandle(transport, request, response);
			// end manual inline (pathentic attempt to reduce stack depth)

		} finally {
			if (access != null) {
				sessionManager.complete(access);
			}
			
			Session session = request.getSession(false);
			if (session != null && oldSession == null && session != access)
				sessionManager.complete(session);

			if (oldSessionManager != null && oldSessionManager != sessionManager) {
				request.setSessionManager(oldSessionManager);
				request.setSession(oldSession);
			}
		}
	}

	@Override
	public void doHandle(Transport transport, Request request, Response response) throws IOException, ServiceException  {
		// start manual inline of nextHandle(transport,request,response);
		if (never())
			nextHandle(transport, request, response);
		else if (nextScope != null && nextScope == handler)
			nextScope.doHandle(transport, request, response);
		else if (handler != null)
			handler.handle(transport, request, response);
		// end manual inline
	}


	/**
	 * @param listener
	 */
	public void addEventListener(EventListener listener) {
		if (sessionManager != null)
			sessionManager.addEventListener(listener);
	}


	public void clearEventListeners() {
		if (sessionManager != null)
			sessionManager.clearEventListeners();
	}
}
