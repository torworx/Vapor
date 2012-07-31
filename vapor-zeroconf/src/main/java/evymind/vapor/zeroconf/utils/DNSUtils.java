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

    public static String qualify(String serviceType, String domain) {
        if (serviceType == null) {
            return null;
        }
        String domainToUse = "";
        if (domain != null && !domain.endsWith(".")) {
            domainToUse = domain + ".";
        } else {
            domainToUse = domain;
        }
        return serviceType.endsWith(domainToUse) ? serviceType : serviceType + domainToUse;

    }
}
