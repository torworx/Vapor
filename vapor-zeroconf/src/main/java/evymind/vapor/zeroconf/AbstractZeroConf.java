package evymind.vapor.zeroconf;

/**
 * Copyright 2012 EvyMind.
 */
public abstract class AbstractZeroConf {

    private String domain;

    private ZeroConfStrategy strategy;

    protected AbstractZeroConf() {
        this.strategy = ZeroConfStrategy.AUTO;
        setDomain("local");
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public ZeroConfStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(ZeroConfStrategy strategy) {
        this.strategy = strategy;
    }
}
