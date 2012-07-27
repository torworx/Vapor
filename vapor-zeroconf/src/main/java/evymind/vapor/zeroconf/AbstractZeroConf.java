package evymind.vapor.zeroconf;

/**
 * Copyright 2012 EvyMind.
 */
public abstract class AbstractZeroConf {

    private String domain;

    private ZeroConfEngine engine;

    protected AbstractZeroConf() {
        this.engine = ZeroConfEngine.AUTO;
        setDomain("local");
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
        if (this.domain != null && !this.domain.endsWith(".")) {
            this.domain += ".";
        }
    }

    public ZeroConfEngine getEngine() {
        return engine;
    }

    public void setEngine(ZeroConfEngine engine) {
        this.engine = engine;
    }
}
