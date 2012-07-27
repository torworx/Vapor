package evymind.vapor.zeroconf.utils;

import javax.jmdns.JmDNS;
import java.io.IOException;

/**
 * Copyright 2012 EvyMind.
 */
public final class DNSUtils {

    private DNSUtils() {
    }

    public static boolean checkDNSFunctionsSilent() {
        try {
            checkDNSFunctions();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void checkDNSFunctions() throws IOException {
        JmDNS.create().close();
    }
}
