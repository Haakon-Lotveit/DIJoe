package test.no.javabook.dijoe.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
//import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import no.javabook.dijoe.DIJoe;
import no.javabook.dijoe.annotations.Username;
import no.javabook.dijoe.exceptions.NoBoundConstructorException;
import no.javabook.dijoe.exceptions.NoBoundSingletonException;
import no.javabook.dijoe.exceptions.NoClassBoundException;
import test.no.javabook.dijoe.testclasses.BasicBindingClass;
import test.no.javabook.dijoe.testclasses.BasicRecursiveDependency;
import test.no.javabook.dijoe.testclasses.RecursiveDependency;
import test.no.javabook.dijoe.testclasses.TestBindingInterface;
import test.no.javabook.dijoe.testclasses.ThingWithUsername;

public class DIJoeTest {

	DIJoe diJoe = null;

	@Before
	public void setUp() throws Exception {
		diJoe = new DIJoe();
	}

	@After
	public void tearDown() throws Exception {
		diJoe = null;
	}

	@Test
	public void testSingletonBinding() {
		String singleton = "I am a singleton, short and stout, here is my handle, here is my trout";
		diJoe.bind(String.class, singleton);
		
		String output = diJoe.resolve(String.class);
		
		assertEquals(singleton, output);
		assertTrue(singleton == output);
	}

	@Test
	public void testNamedSingltonBindings() {
		String singletonName = "Aragorn"; 
		String aragorn = "" +
				"All that is gold does not glitter,\n" + 
				"Not all those who wander are lost;\n" + 
				"The old that is strong does not wither,\n" + 
				"Deep roots are not reached by the frost.\n" + 
				"";
		diJoe.bind(String.class, aragorn, singletonName);
		
		assertTrue(aragorn == diJoe.resolve(String.class, singletonName));
		
		aragorn = "" +
		 "From the ashes a fire shall be woken,\n" + 
		 "A light from the shadows shall spring;\n" + 
		 "Renewed shall be blade that was broken,\n" + 
		 "The crownless again shall be king.\n";
		
		diJoe.bind(String.class, aragorn, singletonName);
		
		assertTrue(aragorn == diJoe.resolve(String.class, singletonName));
	}
	
	@Test
	public void testBasicBinding() {
		diJoe.bind(TestBindingInterface.class, BasicBindingClass.class);
		TestBindingInterface result = diJoe.resolve(TestBindingInterface.class);

		boolean sameClass = BasicBindingClass.class == result.getClass();
		assertTrue(sameClass);
	}

	@Test
	public void testBindingClassesWithDependencies() {
		diJoe.bind(TestBindingInterface.class, BasicBindingClass.class);
		diJoe.bind(RecursiveDependency.class, BasicRecursiveDependency.class);

		TestBindingInterface result = diJoe.resolve(TestBindingInterface.class);

		assertTrue(result.getClass() == BasicBindingClass.class); // Might happen somehow if we mess around with a bunch of classloaders and OSGi and stuff...
	}

	@Test
	public void testBindAnnotationToSingleton() {		
		// ThingWithUserName is a class with the correct annotations to make this work.
		diJoe.bind(ThingWithUsername.class, ThingWithUsername.class);
		diJoe.bind(String.class, "This is the wrong string");
		diJoe.bind(String.class, String.class);
		
		String expected = "test_username";
		diJoe.bindAnnotation(String.class, expected, Username.class);
		
		String firstTest = diJoe.resolveAnnotation(String.class, Username.class);
		
		assertEquals(expected, firstTest);
		assertTrue(expected == firstTest);
		String actual = diJoe.resolve(ThingWithUsername.class).getUsername();

		assertEquals(expected, actual);
		
		// We must be able to overwrite:
		expected = "another_username";
		diJoe.bindAnnotation(String.class, expected, Username.class);
		actual = diJoe.resolve(ThingWithUsername.class).getUsername();
	}
	
	@Test(expected = NoClassBoundException.class)
	public void testAppropriateErrorUnboundType() {
		diJoe.resolve(ThingWithUsername.class);
	}

	@Test(expected = NoBoundSingletonException.class)
	public void testAppropriateErrorUnboundSingleton() {
		diJoe.resolveSingleton(ThingWithUsername.class);
	}
	
	@Test(expected = NoBoundConstructorException.class)
	public void testAppropriateErrorNoConstructor() {
		diJoe.resolveConstructor(ThingWithUsername.class);
	}
}
