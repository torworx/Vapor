package evymind.vapor.core.message;

import evymind.vapor.core.utils.AsciiUtils;

public class Messages {
	
	/**
	 * <pre>
	 * 45 52 45 4E 56 = "ERENV"
	 * XX XX XX XX    = Length of name envelope
	 * YY ...         = Name of envelope
	 * </pre>
	 */
	public static final byte[] ENVELOPE_SIGNATURE = AsciiUtils.toAsciiBytes("ERENV");
	
	public static final int METADATA_REQUEST_ID_LENGTH = 4;
	public static final byte[] METADATA_REQUEST_ID = AsciiUtils.toAsciiBytes("ERDL");

	public static final int PROBE_REQUEST_ID_LENGTH = 5;
	public static final byte[] PROBE_REQUEST_ID = AsciiUtils.toAsciiBytes("PROBE");

	public static final int PROBE_RESPONSE_ID_LENGTH = 8;
	public static final byte[] PROBE_RESPONSE_ID = AsciiUtils.toAsciiBytes("PROBEDOK");

}
