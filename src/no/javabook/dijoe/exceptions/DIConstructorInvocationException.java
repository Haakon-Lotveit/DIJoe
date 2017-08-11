package no.javabook.dijoe.exceptions;

public class DIConstructorInvocationException extends DIException {

	/**
	 * default
	 */
	private static final long serialVersionUID = 1L;

	public DIConstructorInvocationException(Class<?> requestedClass, String message) {
		super(requestedClass, message);
	}

	public DIConstructorInvocationException(Class<?> requestedClass, Throwable cause) {
		super(requestedClass, cause);
	}

	public DIConstructorInvocationException(Class<?> requestedClass, String message, Throwable cause) {
		super(requestedClass, message, cause);
	}

}
