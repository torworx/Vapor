package evymind.vapor.zeroconf;

/**
 * Copyright 2012 EvyMind.
 */
public interface ZeroConfRegistrationListener {

    void registrationFailed(ZeroConfRegistration registration, ZeroConfStrategy strategy, Exception exception);

    void registrationSucceeded(ZeroConfRegistration registration, ZeroConfStrategy strategy);
}
