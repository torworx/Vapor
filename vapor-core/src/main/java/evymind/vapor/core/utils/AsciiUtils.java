package evymind.vapor.core.utils;

import java.nio.charset.Charset;

public class AsciiUtils {
	
    /**
     * 7-bit ASCII, as known as ISO646-US or the Basic Latin block of the
     * Unicode character set
     */
    public static final Charset US_ASCII = Charset.forName("US-ASCII");

	public static byte[] toAsciiBytes(String s) {
		if (s != null) {
			return s.getBytes(US_ASCII);
		}
		return null;
	}
}
