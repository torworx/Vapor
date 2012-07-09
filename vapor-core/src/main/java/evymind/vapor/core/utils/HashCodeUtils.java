package evymind.vapor.core.utils;

import com.google.common.base.Objects;

public class HashCodeUtils {
	
	public static int build(Object...objects) {
		return Objects.hashCode(objects);
	}

}
