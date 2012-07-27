package evymind.vapor.zeroconf;

/**
 * Copyright 2012 EvyMind.
 */
public class ZeroConfRegistrationException extends RuntimeException {

    public ZeroConfRegistrationException() {
    }

    public ZeroConfRegistrationException(String s) {
        super(s);
    }

    public ZeroConfRegistrationException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ZeroConfRegistrationException(Throwable throwable) {
        super(throwable);
    }
}
