package evymind.vapor.examples.echo.impl;

import evymind.vapor.examples.echo.api.EchoService;

/**
 * Copyright 2012 EvyMind.
 */
public class EchoServiceImpl implements EchoService {

    @Override
    public String echo(String info) {
        return info;
    }
}
