package evymind.vapor.bootstrap;

import static org.hamcrest.Matchers.*;
import org.junit.Assert;
import org.junit.Test;

public class CommandLineBuilderTest {
	@Test
	public void testSimpleCommandline() {
		CommandLineBuilder cmd = new CommandLineBuilder("java");
		cmd.addEqualsArg("-Djava.io.tmpdir", "/home/java/temp dir/");
		cmd.addArg("--version");

		Assert.assertThat(cmd.toString(), is("java -Djava.io.tmpdir=/home/java/temp\\ dir/ --version"));
	}

	@Test
	public void testQuotingSimple() {
		assertQuoting("/opt/vapor", "/opt/vapor");
	}

	@Test
	public void testQuotingSpaceInPath() {
		assertQuoting("/opt/vapor 7/home", "/opt/vapor\\ 7/home");
	}

	@Test
	public void testQuotingSpaceAndQuotesInPath() {
		assertQuoting("/opt/vapor 7 \"special\"/home", "/opt/\\ 7\\ \\\"special\\\"/home");
	}

	private void assertQuoting(String raw, String expected) {
		String actual = CommandLineBuilder.quote(raw);
		Assert.assertThat("Quoted version of [" + raw + "]", actual, is(expected));
	}
}
