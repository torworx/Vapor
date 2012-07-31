package evymind.vapor.zeroconf;

/**
 * Copyright 2012 EvyMind.
 */
public class ZeroConfException extends RuntimeException {

    public ZeroConfException() {
    }

    public ZeroConfException(String s) {
        super(s);
    }

    public ZeroConfException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ZeroConfException(Throwable throwable) {
        super(throwable);
    }
}
