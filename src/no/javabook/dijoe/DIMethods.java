package no.javabook.dijoe;

import java.lang.annotation.Annotation;

/**
 * This is just here to help migrate stuff from DIJoe 1 to DIJoe 2.
 * Just contains the methods we want.
 */
public interface DIMethods {
	public <T> void bind(Class<? super T> referenceClass, Class<T> implementingClass);
	public <T> void bind(Class<? super T> referenceClass, T singleton);
	public <T> void bind(Class<? super T> referenceClass, T singleton, String singletonName);
	
	public <T> T resolve(Class<? super T> requested);
	public <T> T resolve(Class<? super T> requested, String singletonName);
	
	public <T> T resolveConstructor(Class<? super T> requested);
	
	public <T> T resolveSingleton(Class<? super T> requested);
	public <T> T resolveSingleton(Class<? super T> requested, String singletonName);
	
	
	/* 
	 * These are not supposed to exist yet, but will.
	 * At the same time, we may want to remove the named-singleton support,
	 * but that's not something we're sure about.
	 */
	public <T> void bindAnnotation(Class<? super T> referenceClass, Class<T> resolvingClass, Class<Annotation> annotation);
	public <T> void bindAnnotation(Class<? super T> referenceClass, T singleton, Class<? extends Annotation> annotation);
	public <T> T resolveAnnotation(Class<? super T> requestedType, Class<? extends Annotation> annotation);
}
