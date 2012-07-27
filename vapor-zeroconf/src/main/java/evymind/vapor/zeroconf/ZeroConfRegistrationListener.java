package evymind.vapor.zeroconf;

/**
 * Copyright 2012 EvyMind.
 */
public interface ZeroConfRegistrationListener {

    void registrationFailed(ZeroConfRegistration registration, ZeroConfEngine engine, Exception exception);

    void registrationSucceeded(ZeroConfRegistration registration, ZeroConfEngine engine);
}
