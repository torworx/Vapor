package evymind.vapor.zeroconf;

/**
 * Copyright 2012 EvyMind.
 */
public interface ZeroConfResolver {

    void resolve(ZeroConfService confService, int timeout);

    boolean tryResolve(ZeroConfService confService, int timeout);
}
