package no.haakon.wsdif;

import static java.util.Objects.isNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class DIJoe {
	private static final String standardSingletonName = "default";
	Map<Class<?>, Constructor<?>> constructors;
	Map<Class<?>, Map<String, Object>> singletons;
	public DIJoe() {
		this.constructors = new HashMap<>();
		this.singletons = new LinkedHashMap<>();
	}

	public <T> void bindSingleton(Class<? super T> interfaceClass, T singleton) {
		bindSingleton(interfaceClass, singleton, standardSingletonName);		
	}

	public <T> void bindSingleton(Class<? super T> interfaceClass, T singleton, String name) {
		singletons.putIfAbsent(interfaceClass, new LinkedHashMap<String, Object>());
		singletons.get(interfaceClass).put(name, singleton);
	}

	public <T> void bindClass(Class<? super T> interfaceClass, Class<T> implementation) {
		boolean isEmpty = false;
		Constructor<?> chosenConstructor = null;
		for(Constructor<?> constructor : implementation.getConstructors()) {
			if(isNull(chosenConstructor) && emptyConstructor(constructor)) {
				chosenConstructor = constructor;
				continue;
			}

			// if there's no constructor, or it's an empty constructor.
			if((isNull(chosenConstructor) || isEmpty) && checkConstructorAnnotations(constructor)) {
				isEmpty = false;
				chosenConstructor = constructor;
				break; // If we have found an annotated constructor we're done with our search.
			}
		}

		if(isNull(chosenConstructor)) {
			String errorMessage = String.format("Error when binding %s to %s: No suitable constructor found (One with no args or annotated with @Inject)",
					interfaceClass.getCanonicalName(), implementation.getCanonicalName());
			throw new NoSuchClassBoundException(errorMessage);
		}

		constructors.put(interfaceClass, chosenConstructor);

	}

	private static boolean emptyConstructor(Constructor<?> constructor) {
		return 0 == constructor.getParameterCount();
	}

	private static boolean checkConstructorAnnotations(Constructor<?> constructor) {
		for(Annotation annotation : constructor.getAnnotations()) {
			if(annotation.annotationType() == Inject.class) {
				return true;
			}
		}
		return false;
	}

	private String getPreferredSingletonName(Annotation[] annotations) {
		for(Annotation a : annotations) {
			if(a instanceof InjectSingleton) {
				return ((InjectSingleton) a).value();
			}
		}

		return null;
	}

	<T> T resolveConstructor(Constructor<T> constructor) {
		Object[] args = new Object[constructor.getParameterCount()];
		Class<?>[] types = constructor.getParameterTypes();

		for(int i = 0; i < types.length; ++i) {
			try { 
				String singletonName = getPreferredSingletonName(constructor.getParameterAnnotations()[i]);
				if(null != singletonName) {
					args[i] = resolveSingleton(types[i], singletonName);
				}
				else {
					args[i] = resolve(types[i]);
				}
			}
			catch(NoSuchClassBoundException sub) {
				String message = String.format("Error while trying to resolve constructor %s: When resolving argument %d of type %s, no such class bound", constructor, i, types[i].getCanonicalName());
				throw new NoSuchClassBoundException(message, sub);
			}
		}

		try {
			return constructor.newInstance(args);
		} catch ( InstantiationException 
				| IllegalAccessException 
				| IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException("couldn't invoke constructor in resolveConstructor method: ", e);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T resolveSingleton(Class<?> resolveMe, String instanceName) {
		if(!singletons.containsKey(resolveMe)) {
			throw new NoSuchClassBoundException("No singletons have been bound for class " + resolveMe.getCanonicalName());
		}
		if(!singletons.get(resolveMe).containsKey(instanceName)) {
			throw new NoSuchElementException("No singleton with name " + instanceName + " has been bound for class " + resolveMe.getCanonicalName());
		}
		return (T) singletons.get(resolveMe).get(instanceName);
	}

	private <T> boolean hasSingleton(Class<T> ofClass, String instanceName) {
		return singletons.getOrDefault(ofClass, Collections.emptyMap()).containsKey(instanceName);
	}

	<T> T resolve(Class<T> resolveMe) {
		return resolve(resolveMe, standardSingletonName);
	}

	@SuppressWarnings("unchecked") 
	<T> T resolve(Class<T> resolveMe, String instanceName) {
		if(hasSingleton(resolveMe, instanceName)) {
			return resolveSingleton(resolveMe, instanceName);
		}

		if(constructors.containsKey(resolveMe)) {
			return (T) resolveConstructor(constructors.get(resolveMe));
		}
		else {
			throw new NoSuchClassBoundException("No class " + resolveMe + " registered!");
		}
	}

	// This is what passes for testing in this quickly thrown together thing.
	public static void main(String[] args) throws IOException {
		DIJoe di = new DIJoe();

		File tmpFile = File.createTempFile("test-", ".log");
		System.out.println("Temp file at: " + tmpFile.getAbsolutePath());
		PrintStream ps = new PrintStream(tmpFile);
		di.bindSingleton(String.class, "Hello, World!", "std.greeting");
		di.bindSingleton(PrintStream.class, ps, "tmpfile");
		di.bindSingleton(PrintStream.class, System.out);
		di.bindClass(HelloWorld.class, PrintStreamHelloWorld.class);

		HelloWorld hw  = di.resolve(HelloWorld.class);

		System.out.println("We can do this: " + di.resolve(String.class, "std.greeting"));
		hw.greet();
	}
}
