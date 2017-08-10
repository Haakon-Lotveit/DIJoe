package no.haakon.wsdif;

public class StdOutHelloWorld implements HelloWorld {

	@Inject
	public StdOutHelloWorld() {
		super();
	}
	
	@Override
	public void greet() {
		System.out.println("Hello World from " + this.getClass().getName());
	}
	
}
