package test.no.javabook.dijoe.testclasses;

import no.javabook.dijoe.annotations.Inject;

public class BindingClassWithDependency implements TestBindingInterface {

	private final RecursiveDependency dependency;
	
	@Inject
	public BindingClassWithDependency(RecursiveDependency dependency) {
		this.dependency = dependency;
	}

	@Override
	public String sayHello() {
		return dependency.getGreeting();
	}

}
