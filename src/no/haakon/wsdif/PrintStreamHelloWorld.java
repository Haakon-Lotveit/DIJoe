package no.haakon.wsdif;

import java.io.PrintStream;

public class PrintStreamHelloWorld implements HelloWorld {
	private final PrintStream printer;
	
	@Inject
	public PrintStreamHelloWorld(@InjectSingleton("tmpfile") PrintStream printer) {
		this.printer = printer;
	}

	@Override
	public void greet() {
		printer.print("Hello world from " + this.getClass().getName());
	}

}
