package evymind.vapor.examples.stcc.client.utils;

public class HexUtils {
    
	public static String radom(int len) {
		String s = Integer.toHexString((int) System.nanoTime());
		return len > s.length() ? s : s.substring(s.length() - len);
	}
}
