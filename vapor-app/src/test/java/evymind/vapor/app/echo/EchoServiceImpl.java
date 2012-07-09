package evymind.vapor.app.echo;

public class EchoServiceImpl implements EchoService {

	@Override
	public String echo(String msg) {
		return msg;
	}

}
