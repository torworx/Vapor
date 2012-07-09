package evymind.vapor.tests.app;

public class SimpleInfoService implements InfoService {

	@Override
	public String getInfo() {
		return "FooService-1 Responding";
	}

}
