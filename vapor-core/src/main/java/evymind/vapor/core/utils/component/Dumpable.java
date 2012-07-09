package evymind.vapor.core.utils.component;

import java.io.IOException;

public interface Dumpable {

	String dump();

	void dump(Appendable out, String indent) throws IOException;

}
