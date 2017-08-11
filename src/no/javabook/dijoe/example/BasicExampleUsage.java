package no.javabook.dijoe.example;

import no.javabook.dijoe.DIJoe;
import no.javabook.dijoe.annotations.Username;
import test.no.javabook.dijoe.testclasses.BasicRecursiveDependency;
import test.no.javabook.dijoe.testclasses.BindingClassWithDependency;
import test.no.javabook.dijoe.testclasses.RecursiveDependency;
import test.no.javabook.dijoe.testclasses.TestBindingInterface;
import test.no.javabook.dijoe.testclasses.ThingWithUsername;

/**
 * The point of this class is to show some example usage of DI Joe.
 */
public class BasicExampleUsage {
	
	public static void main(String[] args) {
		// First we must instantiate an object.
		DIJoe di = new DIJoe();
		// Then we can wire in objects, these are taken from the unit-tests.
		
		// Notice that we can tell DI Joe to give us objects of the same class. We don't have to use interfaces.
		// This can be useful if you have to work with code that isn't as nice and neat as it should be.
		di.bind(ThingWithUsername.class, ThingWithUsername.class);

		// If you look at ThingWithUsername, the constructor takes a string. We can either bind a directly:
		di.bind(String.class, "username_1");
		
		ThingWithUsername user1 = di.resolve(ThingWithUsername.class);
		System.out.println(user1);	
		
		// But we can also use the @Username annotation to our advantage and bind an instance to that.
		// This is not properly tested yet, so it might be a bit fragile.
		di.bindAnnotation(String.class, "username_2", Username.class);
		ThingWithUsername user2 = di.resolve(ThingWithUsername.class);
		System.out.println(user2);

		// It will of course, handle nested dependencies if you have them, so long as you have wired it to handle all the types it will need to inject.
		di.bind(TestBindingInterface.class, BindingClassWithDependency.class);
		
		// We can't tell DI Joe to resolve this, because we haven't told it how to resolve the devendencies of BindingClassWithDepenceny yet.
		// You have to be completely accurate here, it's not enough to bind a subtype, you have to bind the actual type.
		// That may be changed in a future release though.		
		di.bind(RecursiveDependency.class, BasicRecursiveDependency.class);
		
		// Now that all the parts have been successfully added, we can tell it to resolve our dependencies.
		TestBindingInterface greeter = di.resolve(TestBindingInterface.class);

		// And finally, let the greeter greet you. ^_^
		System.out.println(greeter.sayHello());
		
		/*
		 * If all goes as it should you should have these 3 lines:
		 * test.no.javabook.dijoe.testclasses.ThingWithUsername<username=username_1>
		 * test.no.javabook.dijoe.testclasses.ThingWithUsername<username=username_2>
		 * hello
		 */
	}

}
