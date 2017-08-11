package no.javabook.dijoe;

import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import no.javabook.dijoe.exceptions.DIConstructorInvocationException;
import no.javabook.dijoe.exceptions.NoBoundConstructorException;
import no.javabook.dijoe.exceptions.NoBoundSingletonException;
import no.javabook.dijoe.exceptions.NoClassBoundException;
import no.javabook.dijoe.exceptions.NoSuitableConstructorException;

/**
 * This is possibly the worst DI framework out there that actually works.
 * However, it works.
 * 
 * It does lack some features, and it needs more tests.
 * So:
 * TODO: More tests
 * TODO: Let me bind annotations to constructors
 * TODO: Let me have an annotations that tell DIJoe to inject a specific singleton (similar to the annotation-based approach, but without the type safety)
 *
 * I think that anything else would be far, faaar out of scope.
 */
public class DIJoe implements DIMethods {
	public static final String DEFAULT_SINGLETON_NAME = "default";

	private final Map<Class<?>, Constructor<?>> constructors;
	private final Map<Class<?>, Map<String, Object>> singletons;
	private final Map<Class<?>, Map<Class<? extends Annotation>, Object>> annotatedSingletons;

	public DIJoe() {
		constructors = new HashMap<>();
		singletons = new HashMap<>();
		annotatedSingletons = new HashMap<>();
	}

	@Override
	public <T> void bind(Class<? super T> desiredType, Class<T> injectedType) {
		boolean isEmpty = false;
		Constructor<?> chosenConstructor = null;
		for(Constructor<?> constructor : injectedType.getConstructors()) {
			if(isNull(chosenConstructor) && emptyConstructor(constructor)) {
				chosenConstructor = constructor;
				continue;
			}

			// if there's no constructor, or it's an empty constructor.
			if((isNull(chosenConstructor) || isEmpty) && isConstructorInjectable(constructor)) {
				isEmpty = false;
				chosenConstructor = constructor;
				break; // If we have found an annotated constructor we're done with our search.
			}
		}

		if(isNull(chosenConstructor)) {
			String errorMessage = String.format("Error when binding %s to %s: No suitable constructor found (One with no args or annotated with @Inject)",
					desiredType.getCanonicalName(), injectedType.getCanonicalName());
			throw new NoBoundConstructorException(desiredType, errorMessage);
		}

		constructors.put(desiredType, chosenConstructor);

	}

	@Override
	public <T> void bind(Class<? super T> desiredType, T singleton) {
		this.bind(desiredType, singleton, DEFAULT_SINGLETON_NAME);		
	}

	@Override
	public <T> void bind(Class<? super T> desiredType, T singleton, String singletonName) {
		if(!singletons.containsKey(desiredType)) {
			singletons.put(desiredType, new HashMap<>());
		}
		singletons.get(desiredType).put(singletonName, singleton);
	}

	@Override
	public <T> T resolve(Class<? super T> requestedType) {
		return resolve(requestedType, DEFAULT_SINGLETON_NAME);
	}

	@Override
	public <T> T resolve(Class<? super T> requestedType, String singletonName) {
		Class<? extends Annotation> annotation = null;
		if(hasSingleton(requestedType, singletonName)) {
			return resolveSingleton(requestedType, singletonName);
		}		
		else if(hasConstructor(requestedType)) {
			return resolveConstructor(requestedType);
		}
		// Checking the annotations for suitable types is a measure of last resort. We're not going to do that well.
		else if(null != (annotation = getARegisteredAnnotationOrNull(requestedType))){
			return resolveAnnotation(requestedType, annotation);
		}
		else {
			throw new NoClassBoundException(requestedType);
		}
	}

	@Override
	public <T> T resolveConstructor(Class<? super T> requestedType) {
		@SuppressWarnings("unchecked") // We only put constructors that create subtypes of T into our maps.
		Constructor<T> constructor = (Constructor<T>) constructors.get(requestedType);
		if(null == constructor) {
			String errorMessage = String.format("No constructor have been bound for type <%s>", requestedType.getCanonicalName());
			throw new NoBoundConstructorException(requestedType, errorMessage);
		}

		Object[] args = new Object[constructor.getParameterCount()];
		Class<?>[] types = constructor.getParameterTypes();
		Annotation[][] annotations = constructor.getParameterAnnotations();

		for(int i = 0; i < types.length; ++i) {
			try {
				// This is ugly. Anyone have any good ideas to make this better?
				// It's explicit, but it's C-in-1974 levels of hacky.
				if((args[i] = getAnnotationOrNull(types[i], annotations[i])) == null) {
					if(hasSingleton(types[i])) {
						args[i] = resolveSingleton(types[i]);
					}
					else {
						// finally, attempt to solve via constructor
						args[i] = resolveConstructor(types[i]);
					}
				}
			}
			catch(Throwable sub) {
				String message = String.format("Error while trying to resolve constructor %s: When resolving argument %d of type %s, no such class bound", constructor.toString(), i, types[i].getCanonicalName());
				throw new NoClassBoundException(requestedType, message, sub);
			}
		}
		try {
			return (T) constructor.newInstance(args);
		} catch ( InstantiationException 
				| IllegalAccessException 
				| IllegalArgumentException
				| InvocationTargetException e) {
			throw new DIConstructorInvocationException(requestedType, "couldn't invoke constructor in resolveConstructor method: ", e);
		}
	}

	@Override
	public <T> T resolveSingleton(Class<? super T> requestedType) {
		try {
			return resolveSingleton(requestedType, DEFAULT_SINGLETON_NAME);
		}
		catch(NoBoundSingletonException e) {
			String errorMsg = String.format("No singleton with default name bound for class <%s>", requestedType.getCanonicalName());
			throw new NoBoundSingletonException(requestedType, errorMsg, e);
		}
	}

	@Override
	public <T> T resolveSingleton(Class<? super T> requestedType, String singletonName) {
		@SuppressWarnings("unchecked")
		T out = (T) singletons.getOrDefault(requestedType, Collections.emptyMap()).get(singletonName);
		if(isNull(out)) {
			String errorMsg = String.format("No singleton with name <%s> bound for type <%s>", singletonName, requestedType.getCanonicalName());
			throw new NoBoundSingletonException(requestedType, errorMsg);
		}
		return out;
	}

	// TBQH I'm not yet certain if this is a good idea.
	@Override
	public <T> void bindAnnotation(Class<? super T> desiredType, Class<T> injectedType, Class<Annotation> annotation) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public <T> void bindAnnotation(Class<? super T> referenceClass, T singleton, Class<? extends Annotation> annotation) {
		if(!annotatedSingletons.containsKey(referenceClass)) {
			annotatedSingletons.put(referenceClass, new HashMap<>());
		}
		Map<Class<? extends Annotation>, Object> annotationMap = annotatedSingletons.get(referenceClass);

		annotationMap.put(annotation, singleton);
	}

	@Override
	public <T> T resolveAnnotation(Class<? super T> requestedType, Class<? extends Annotation> annotation) {
		@SuppressWarnings("unchecked")
		T out = (T) annotatedSingletons.getOrDefault(requestedType, emptyMap()).get(annotation);
		if(isNull(out)) {
			String errorMessage = String.format("No singleton registered for class <%s> with annotation <%s>", requestedType.getCanonicalName(), annotation.getCanonicalName());
			throw new NoSuitableConstructorException(requestedType, errorMessage);
		}
		else {
			return out;
		}
	}



	// Here be helper methods

	/**
	 * This tells us if our store has a singleton stored for our class.
	 * @param requested the class to look up.
	 * @return an annotation that has a registered singleton, with no guarantee of which one it will return, only that it is valid.
	 */
	private Class<? extends Annotation> getARegisteredAnnotationOrNull(Class<?> requested) {
		Map<Class<? extends Annotation>, Object> annotationMap = annotatedSingletons.get(requested);
		if(annotationMap == null || annotationMap.size() == 0) {
			return null;
		}
		return annotationMap.entrySet().iterator().next().getKey();
	}

	private boolean registeredAnnotationForClass(Class<?> annotatedClass, Annotation anno) {
		return annotatedSingletons.getOrDefault(annotatedClass, emptyMap()).get(anno.annotationType()) != null;
	}

	private Object getAnnotationOrNull(Class<?> argumentType, Annotation[] annotations) {
		for(Annotation parameterAnnotation : annotations) {
			if(registeredAnnotationForClass(argumentType, parameterAnnotation)) {
				return resolveAnnotation(argumentType, parameterAnnotation.annotationType());
			}
		}

		return null;
	}

	private boolean hasSingleton(Class<?> ofClass) {
		return hasSingleton(ofClass, DEFAULT_SINGLETON_NAME);
	}

	private boolean hasSingleton(Class<?> ofClass, String instanceName) {
		return singletons.getOrDefault(ofClass, Collections.emptyMap()).containsKey(instanceName);
	}

	private boolean hasConstructor(Class<?> forClass) {
		return constructors.containsKey(forClass);
	}

	private static boolean emptyConstructor(Constructor<?> constructor) {
		return 0 == constructor.getParameterCount();
	}

	private static boolean isConstructorInjectable(Constructor<?> constructor) {
		for(Annotation annotation : constructor.getAnnotations()) {
			if(annotation.annotationType() == no.javabook.dijoe.annotations.Inject.class) {
				return true;
			}
		}
		return false;
	}


}
